package com.rdutta.orderservice.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private String orderId;
    private String product;
    private int quantity;
}