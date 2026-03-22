package com.rdutta.paymentservice.kafka.producer;

import com.rdutta.paymentservice.events.PaymentFailedEvent;
import com.rdutta.paymentservice.events.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSuccess(String orderId) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>("payment-success-topic",
                        new PaymentSuccessEvent(orderId));

        addHeaders(record);
        kafkaTemplate.send(record);
    }

    public void sendFailure(String orderId) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>("payment-failed-topic",
                        new PaymentFailedEvent(orderId));

        addHeaders(record);
        kafkaTemplate.send(record);
    }

    private void addHeaders(ProducerRecord<String, Object> record) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            record.headers().add("correlationId", correlationId.getBytes());
        }
    }
}