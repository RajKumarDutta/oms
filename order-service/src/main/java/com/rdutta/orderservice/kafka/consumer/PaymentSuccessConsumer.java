package com.rdutta.orderservice.kafka.consumer;

import com.rdutta.orderservice.entity.Order;
import com.rdutta.orderservice.constants.OrderStatus;
import com.rdutta.orderservice.events.PaymentSuccessEvent;
import com.rdutta.orderservice.repository.OrderRepository;

import com.rdutta.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessConsumer {

    private final OrderService orderService;

    @Transactional
    @KafkaListener(topics = "payment-success-topic", groupId = "order-group")
    public void consume(PaymentSuccessEvent event,
                        @Header(name = "correlationId", required = false) String correlationId) {

        if (correlationId != null) MDC.put("correlationId", correlationId);

        try {
            orderService.confirmOrder(event.getOrderId());
        } finally {
            MDC.clear();
        }
    }
}