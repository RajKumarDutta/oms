package com.rdutta.orderservice.events;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private String orderId;
}