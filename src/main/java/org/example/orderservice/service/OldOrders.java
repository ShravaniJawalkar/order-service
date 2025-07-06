package org.example.orderservice.service;

import org.example.orderservice.repository.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("oldOrder")
public class OldOrders implements Order {
    @Override
    public String orderType() {
        return "old order";
    }
}
