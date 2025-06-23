package org.example.orderservice.service;


import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dao.*;
import org.example.orderservice.repository.OrderItemsRepository;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) {
        ResponseEntity<OrderResponse> response = validateRequest(orderRequest);
        if (response.getStatusCode() != HttpStatus.OK) {
            return response;
        }
        // Create and save the order
        Orders order = Orders.builder()
                .userId(orderRequest.getUserId())
                .totalAmount(orderRequest.getPrice().multiply(BigDecimal.valueOf(orderRequest.getQuantity())))
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        OrderItems orderItems = OrderItems.builder()
                .price(orderRequest.getPrice())
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .totalPrice(orderRequest.getPrice().multiply(BigDecimal.valueOf(orderRequest.getQuantity())))
                .build();

        order.addItem(orderItems);
        orderItems.setOrders(order);
        orderItemsRepository.save(orderItems);
        if (order.getId() == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // Update the product quantity in the ProductService
        updateProductQuantity(orderRequest, order);
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(order.getId());
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    private void updateProductQuantity(OrderRequest orderRequest, Orders order) {
        ResponseEntity<ProductResponse> response = productServiceClient.getProductDetails(orderRequest.getProductId());
        ProductResponse product = Objects.requireNonNull(response.getBody());
        orderRequest.setQuantity(product.getQuantity() - orderRequest.getQuantity());
        ResponseEntity<String> productResponse = productServiceClient.updateProductQuantity(orderRequest);
        if (productResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to update product quantity for productId: {}", orderRequest.getProductId());
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product quantity");
        }

    }

    private ResponseEntity<OrderResponse> validateRequest(OrderRequest orderRequest) {
        // Validate if user exists in UserService
        HttpStatusCode userStatus = validateUser(orderRequest.getUserId());
        //Validate if product exists in ProductService
        HttpStatusCode productStatus = existsByProductId(orderRequest);
        return (userStatus.isSameCodeAs(HttpStatus.OK) && productStatus.isSameCodeAs(HttpStatus.OK)) ?
                new ResponseEntity<>(HttpStatus.OK) :
                new ResponseEntity<>(userStatus.isSameCodeAs(HttpStatus.OK) ?
                        productStatus : userStatus);

    }

    private HttpStatusCode validateProductAndPriceQuantity(ProductResponse productResponse, OrderRequest orderRequest) {
        //compare product price and quantity
        if (orderRequest.getPrice().compareTo(BigDecimal.valueOf(productResponse.getPrice())) != 0) {
            return HttpStatus.BAD_REQUEST;
        }
        if (orderRequest.getQuantity() <= 0 || productResponse.getQuantity().compareTo(orderRequest.getQuantity()) < 0) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.OK;
    }

    private HttpStatusCode existsByProductId(OrderRequest orderRequest) {
        try {
            ResponseEntity<ProductResponse> response = productServiceClient.getProductDetails(orderRequest.getProductId());
            if (response.getStatusCode() == HttpStatus.OK) {
                ProductResponse productResponse = response.getBody();
                return productResponse != null ? validateProductAndPriceQuantity(productResponse, orderRequest) :
                        HttpStatus.NO_CONTENT;
            } else if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                log.warn("Product service is unavailable, don't allow order creation for product: {}", orderRequest.getProductId());
                return HttpStatus.SERVICE_UNAVAILABLE; // Reject order creation when service is down
            } else {
                return HttpStatus.INTERNAL_SERVER_ERROR; // Other status codes mean user validation failed
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Product with ID {} not found", orderRequest.getProductId());
            return HttpStatus.NOT_FOUND; // User does not exist
        } catch (RestClientException e) {
            // This should not happen now because circuit breaker handles it with fallback
            log.error("Unexpected RestClientException for productId {}: {}", orderRequest.getProductId(), e.getMessage());
            return HttpStatus.GATEWAY_TIMEOUT; // or true, depending on your business logic
        } catch (Exception e) {
            // Any other unexpected exception
            log.error("Unexpected exception during user validation for productId {}: {}", orderRequest.getProductId(), e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR; // or true, depending on your business logic
        }

    }


    private HttpStatusCode validateUser(Long userId) {
        try {
            ;
            ResponseEntity<UserServiceResponse> response = userServiceClient.getUserDetail(userId);

            // Handle both successful response and fallback response
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getStatusCode(); // User exists
            } else if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                log.warn("User service is unavailable, don't allow order creation for userId: {}", userId);
                return response.getStatusCode(); // Reject order creation when service is down
            } else {
                return response.getStatusCode(); // Other status codes mean user validation failed
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.info("User with ID {} not found", userId);
            return HttpStatus.NOT_FOUND; // User does not exist
        } catch (RestClientException e) {
            // This should not happen now because circuit breaker handles it with fallback
            log.error("Unexpected RestClientException for userId {}: {}", userId, e.getMessage());
            return HttpStatus.GATEWAY_TIMEOUT; // or true, depending on your business logic
        } catch (Exception e) {
            // Any other unexpected exception
            log.error("Unexpected exception during user validation for userId {}: {}", userId, e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR; // or true, depending on your business logic
        }
    }


    public ResponseEntity<OrderDetails> getOrderById(Long orderId) {
        Optional<Orders> order = orderRepository.findById(orderId);
        OrderDetails orderResponse = new OrderDetails();
        if (order.isEmpty()) {
            return new ResponseEntity<>(orderResponse, HttpStatus.NOT_FOUND);
        } else {
            order.ifPresent(orders -> {
                setOrderResponseDetails(orderResponse, orders);
            });

        }
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    private void setOrderResponseDetails(OrderDetails orderResponse, Orders orders) {
        String userName = getUserName(orders.getUserId());
        if (!userName.equals("404")) {
            orderResponse.setOrderId(orders.getId());
            orderResponse.setUserName(userName);
            orderResponse.setTotalAmount(orders.getTotalAmount());
            orderResponse.setStatus(String.valueOf(orders.getStatus()));
            orderResponse.setOrderDetails(orders.getItems().stream()
                    .map(item -> OrderItemsDetails.builder()
                            .itemId(item.getId())
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .toList());

        }
    }

    private String getUserName(Long userId) {
        try {
            ResponseEntity<UserServiceResponse> response = userServiceClient.getUserDetail(userId);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getUserName();
            } else if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                log.warn("User service unavailable for userId: {}, returning default name", userId);
                return "Service Unavailable"; // Return a default name when service is down
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.error("User with ID {} not found", userId);
        } catch (Exception e) {
            log.error("Error getting user name for userId {}: {}", userId, e.getMessage());
        }

        return String.valueOf(HttpStatus.NOT_FOUND.value());
    }

    @Transactional
    public String updateOrderStatus(long orderId, String status) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Order with ID " + orderId + " does not exist"));

        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        return String.format("Order with ID %d has been updated to status %s", orderId, status);
    }

    public String deleteOrder(long orderId) {
        Optional<Orders> orders = orderRepository.findById(orderId);
        if (orders.isEmpty()) {
            log.error("Order with ID {} does not exist. Status: {}", orderId, HttpStatus.NOT_FOUND);
            return String.format(HttpStatus.NOT_FOUND.toString(), "Order with ID " + orderId + " does not exist");
        } else {
            orderRepository.deleteById(orderId);
            return String.format("Order with ID %d has been deleted", orderId);
        }
    }

    public ResponseEntity<List<OrderDetails>> getOrderByUserId(long userId) {
        List<OrderDetails> orderResponseList = new ArrayList<>();
        List<Orders> orders = orderRepository.findAllByUserId(userId);

        if (orders.isEmpty()) {
            log.error("Order with User ID {} does not exist. Status: {}", userId, HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(orderResponseList, HttpStatus.NOT_FOUND);
        }

        orders.forEach(orderDetails -> {
            OrderDetails orderResponse = new OrderDetails();
            orderResponse.setOrderId(orderDetails.getId());
            orderResponse.setUserName(getUserName(orderDetails.getUserId()));
            orderResponse.setTotalAmount(orderDetails.getTotalAmount());
            orderResponse.setStatus(String.valueOf(orderDetails.getStatus()));
            orderResponse.setOrderDetails(orderDetails.getItems().stream()
                    .map(item -> OrderItemsDetails.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .toList());
            orderResponseList.add(orderResponse);
        });

        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }
}
