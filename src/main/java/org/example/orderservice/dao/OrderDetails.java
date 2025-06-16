package org.example.orderservice.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetails {

    private long orderId;
    private String userName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemsDetails> orderDetails = new ArrayList<>();
}
