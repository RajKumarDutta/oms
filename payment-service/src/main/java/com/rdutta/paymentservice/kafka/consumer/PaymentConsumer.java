package com.rdutta.paymentservice.kafka.consumer;

import com.rdutta.paymentservice.events.OrderCreatedEvent;
import com.rdutta.paymentservice.kafka.producer.PaymentProducer;
import com.rdutta.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentService service;
    private final PaymentProducer producer;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-group")
    public void consume(OrderCreatedEvent event,
                        @Header(name = "correlationId", required = false) String correlationId) {

        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }

        try {
            boolean success = service.processPayment(
                    event.getOrderId(),
                    event.getProduct()
            );

            if (success) {
                producer.sendSuccess(event.getOrderId());
            } else {
                producer.sendFailure(event.getOrderId());
            }

        } finally {
            MDC.clear();
        }
    }
}