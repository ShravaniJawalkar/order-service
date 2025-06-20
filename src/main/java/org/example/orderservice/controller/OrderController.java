package org.example.orderservice.controller;

import jakarta.validation.Valid;
import org.example.orderservice.dao.OrderDetails;
import org.example.orderservice.dao.OrderRequest;
import org.example.orderservice.dao.OrderResponse;
import org.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orders) {
        return orderService.createOrder(orders);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable long orderId, @RequestBody String status) {
        String responseStatus = orderService.updateOrderStatus(orderId, status);
        return new ResponseEntity<>(responseStatus, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetails> getOrder(@PathVariable long orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping("/user/{userId}/orders")
    public ResponseEntity<List<OrderDetails>> getUserOrder(@PathVariable long userId) {
        return orderService.getOrderByUserId(userId);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<String> deleteOrder(@PathVariable long orderId) {
        String response = orderService.deleteOrder(orderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
