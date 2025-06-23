package org.example.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dao.UserServiceResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Service
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    // Constructor for Spring to inject dependencies
    public UserServiceClient(RestTemplate restTemplate, @Value("${services.user.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserDetailFallback")
    public ResponseEntity<UserServiceResponse> getUserDetail(Long userId) {
        String url = userServiceUrl + userId;
        log.info("Calling user service at: {}", url);

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
                    UserServiceResponse userResponse = objectMapper.readValue(response.getBody(), UserServiceResponse.class);
                    return ResponseEntity.ok(userResponse);
                } else {
                    throw new RestClientException("Invalid content type received: " + contentType);
                }
            } else {
                throw new HttpClientErrorException(response.getStatusCode(), "Service returned error status");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calling user service for userId {}: {}", userId, e.getMessage());
            throw new RestClientException("HTTP error: " + e.getMessage(), e);
        } catch (UnknownContentTypeException e) {
            log.error("Content type error calling user service for userId {}: {}", userId, e.getMessage());
            throw new RestClientException("Content type error: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error for userId {}: {}", userId, e.getMessage());
            throw new RestClientException("JSON parsing error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling user service for userId {}: {}", userId, e.getMessage());
            throw new RestClientException("Service call failed: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<UserServiceResponse> getUserDetailFallback(Long userId, Exception ex) {
        log.error("Circuit breaker fallback triggered for user ID {}: {}", userId, ex.getMessage());

        UserServiceResponse defaultResponse = new UserServiceResponse();
        // Set default values - you might want to set some default values here
        // defaultResponse.setUserId(userId);
        // defaultResponse.setUserName("Unknown User");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(defaultResponse);
    }
}