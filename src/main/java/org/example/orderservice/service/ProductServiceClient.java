package org.example.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dao.OrderRequest;
import org.example.orderservice.dao.ProductResponse;
import org.example.orderservice.dao.UserServiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate, @Value("${services.product.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }


    @CircuitBreaker(name = "product-service", fallbackMethod = "getProductDetailsFallback")
    public ResponseEntity<ProductResponse> getProductDetails(@NotNull Long productId) {
        String url = productServiceUrl + "products/" + productId;
        log.info("Calling product service at: {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                if (contentType != null && contentType.contains("application/json")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ProductResponse productResponse = objectMapper.readValue(response.getBody(), ProductResponse.class);
                    return ResponseEntity.ok(productResponse);
                } else {
                    throw new RestClientException("Invalid content type received: " + contentType);
                }
            } else {
                throw new HttpClientErrorException(response.getStatusCode(), "Service returned error status");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calling product service for productId {}: {}", productId, e.getMessage());
            throw new RestClientException("HTTP error: " + e.getMessage(), e);
        } catch (UnknownContentTypeException e) {
            log.error("Content type error calling product service for productId {}: {}", productId, e.getMessage());
            throw new RestClientException("Content type error: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error for productId {}: {}", productId, e.getMessage());
            throw new RestClientException("JSON parsing error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling product service for productId {}: {}", productId, e.getMessage());
            throw new RestClientException("Service call failed: " + e.getMessage(), e);
        }

    }

    public ResponseEntity<ProductResponse> getProductDetailsFallback(Long productId, Throwable throwable) {
        log.error("Circuit breaker fallback triggered for product ID {}: {}", productId, throwable.getMessage());

        // Create a default response or return appropriate error
        ProductResponse defaultResponse = new ProductResponse();
        // Set default values if needed

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(defaultResponse);
        // OR return just the status without body:
        // return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "updateProductQuantityFallback")
    public ResponseEntity<String> updateProductQuantity(OrderRequest orderRequest) {
        String url = productServiceUrl + "products/" + orderRequest.getProductId()+"/quantity";
        log.info("Calling product service at: {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String,Object> variables = new HashMap<>();
            variables.put("quantity",orderRequest.getQuantity());
            ObjectMapper objectMapper = new ObjectMapper();
            // Create the request body
            String requestBody = objectMapper.writeValueAsString(variables);

            HttpEntity<?> requestEntity = new HttpEntity<>(requestBody,headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                if (contentType != null && contentType.contains("application/json")) {
                    return ResponseEntity.ok(response.getBody());
                } else {
                    throw new RestClientException("Invalid content type received: " + contentType);
                }
            } else {
                throw new HttpClientErrorException(response.getStatusCode(), "Service returned error status");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calling product service for product quantity {}: {}", orderRequest.getQuantity(), e.getMessage());
            throw new RestClientException("HTTP error: " + e.getMessage(), e);
        } catch (UnknownContentTypeException e) {
            log.error("Content type error calling product service for product quantity{}: {}", orderRequest.getQuantity(), e.getMessage());
            throw new RestClientException("Content type error: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error for product quantity {}: {}", orderRequest.getQuantity(), e.getMessage());
            throw new RestClientException("JSON parsing error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling product service for product quantity {}: {}", orderRequest.getQuantity(), e.getMessage());
            throw new RestClientException("Service call failed: " + e.getMessage(), e);
        }

    }

    public ResponseEntity<String> updateProductQuantityFallback(OrderRequest orderRequest, Throwable throwable) {
        log.error("Circuit breaker fallback triggered for product quantity {}: {}", orderRequest.getQuantity(), throwable.getMessage());

        // Create a default response or return appropriate error
        String defaultResponse = "Service Unavailable";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(defaultResponse);
        // OR return just the status without body:
        // return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
