package com.rdutta.paymentservice.events;

import lombok.Data;

@Data
public class OrderCreatedEvent {
    private String orderId;
    private String product;
    private int quantity;
}
