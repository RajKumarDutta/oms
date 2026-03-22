package com.rdutta.paymentservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final MeterRegistry meterRegistry;

    public boolean processPayment(String orderId, String product) {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.info("Processing payment for order: {}", orderId);

            // simulate failure case
            if ("iphone-15".equalsIgnoreCase(product)) {
                meterRegistry.counter("payment.failed").increment();
                return false;
            }

            meterRegistry.counter("payment.success").increment();
            return true;

        } finally {
            sample.stop(
                    Timer.builder("payment.processing.time")
                            .register(meterRegistry)
            );
        }
    }
}