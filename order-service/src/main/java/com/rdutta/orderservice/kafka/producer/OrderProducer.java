package com.rdutta.orderservice.kafka.producer;

import com.rdutta.orderservice.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {

        ProducerRecord<String, Object> record =
                new ProducerRecord<>("order-created-topic", event.getOrderId(), event);

        String correlationId = MDC.get("correlationId");

        if (correlationId != null) {
            record.headers().add("correlationId", correlationId.getBytes());
        }

        kafkaTemplate.send(record);

        log.info("Published OrderCreatedEvent");
    }
}