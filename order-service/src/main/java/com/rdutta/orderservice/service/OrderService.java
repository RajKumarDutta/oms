package com.rdutta.orderservice.service;

import com.rdutta.orderservice.client.InventoryClient;
import com.rdutta.orderservice.constants.OrderStatus;
import com.rdutta.orderservice.dto.OrderRequest;
import com.rdutta.orderservice.entity.Order;
import com.rdutta.orderservice.events.OrderCreatedEvent;
import com.rdutta.orderservice.kafka.producer.OrderProducer;
import com.rdutta.orderservice.repository.OrderRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final OrderProducer producer;
    private final InventoryClient inventoryClient;
    private final MeterRegistry meterRegistry;

    // 🔥 Counters (initialized once)
    private Counter successCounter;
    private Counter failureCounter;

    @Transactional
    public void createOrder(OrderRequest request) {

        // Initialize counters lazily (safe + avoids constructor noise)
        if (successCounter == null) {
            successCounter = Counter.builder("order.success")
                    .description("Total successful orders")
                    .register(meterRegistry);

            failureCounter = Counter.builder("order.failure")
                    .description("Total failed orders")
                    .register(meterRegistry);
        }

        // 🔥 Start timer
        Timer.Sample sample = Timer.start(meterRegistry);

        try {

            log.info("Creating order for product: {}", request.getProduct());

            // 🔥 Step 1: gRPC call (Inventory)
            boolean available = inventoryClient.checkStock(request.getProduct());

            if (!available) {
                failureCounter.increment();
                log.warn("Product out of stock: {}", request.getProduct());
                throw new RuntimeException("Product out of stock");
            }

            // 🔥 Step 2: Save order (DB)
            Order order = Order.builder()
                    .orderId(request.getOrderId())
                    .product(request.getProduct())
                    .quantity(request.getQuantity())
                    .status(OrderStatus.CREATED)
                    .build();

            repository.save(order);

            log.info("Order saved in DB with status CREATED");

            // 🔥 Step 3: Publish Kafka event
            OrderCreatedEvent event = new OrderCreatedEvent(
                    request.getOrderId(),
                    request.getProduct(),
                    request.getQuantity()
            );

            producer.publishOrderCreated(event);

            log.info("OrderCreatedEvent published to Kafka");

            // 🔥 Success metric
            successCounter.increment();

        } catch (Exception ex) {

            // 🔥 Failure metric with tag
            meterRegistry.counter("order.failure.reason",
                    "exception", ex.getClass().getSimpleName()).increment();

            log.error("Order creation failed for orderId={} reason={}",
                    request.getOrderId(), ex.getMessage(), ex);

            // ❗ IMPORTANT: rethrow (do NOT swallow)
            throw ex;

        } finally {

            // 🔥 Stop timer
            sample.stop(
                    Timer.builder("order.create.duration")
                            .description("Time taken to create order")
                            .tag("product", request.getProduct())
                            .register(meterRegistry)
            );
        }
    }


    @Transactional
    public void confirmOrder(String orderId) {
        Order order = repository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
        repository.save(order);
        log.info("Order {} confirmed", orderId);
    }
}