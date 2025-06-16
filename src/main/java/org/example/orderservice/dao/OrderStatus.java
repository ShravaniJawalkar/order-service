package org.example.orderservice.dao;

public enum OrderStatus {
    PENDING,     // The order has been created but not yet processed.
    COMPLETED,   // The order has been successfully processed.
    CANCELED,    // The order was canceled.
    FAILED       // The order processing failed.
}