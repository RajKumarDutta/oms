package com.rdutta.orderservice.controller;

import com.rdutta.orderservice.dto.OrderRequest;
import com.rdutta.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody OrderRequest request) {
        service.createOrder(request);
        return ResponseEntity.ok("Order placed successfully!");
    }
}