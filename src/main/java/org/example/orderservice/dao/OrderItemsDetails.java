package org.example.orderservice.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemsDetails {

    private long itemId;
    private int quantity;
    private long productId;
    private BigDecimal totalPrice;
}
