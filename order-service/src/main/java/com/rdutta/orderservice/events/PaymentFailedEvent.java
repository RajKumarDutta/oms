package com.rdutta.orderservice.events;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private String orderId;
}