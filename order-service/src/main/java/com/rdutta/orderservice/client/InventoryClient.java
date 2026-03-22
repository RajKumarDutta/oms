package com.rdutta.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class InventoryClient {

    private final com.grpc.inventory.InventoryServiceGrpc.InventoryServiceBlockingStub baseStub;

    public InventoryClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 6565)
                .usePlaintext()
                .build();

        // Store the base stub without a pre-set deadline
        this.baseStub = com.grpc.inventory.InventoryServiceGrpc.newBlockingStub(channel);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    public boolean checkStock(String product) {
        try {
            com.grpc.inventory.InventoryRequest request = com.grpc.inventory.InventoryRequest.newBuilder()
                    .setProduct(product)
                    .build();

            log.info("Calling inventory-service via gRPC for product={}", product);
            // ✅ Apply the 2-second deadline fresh for every call
            com.grpc.inventory.InventoryResponse response = baseStub
                    .withDeadlineAfter(2, TimeUnit.SECONDS)
                    .checkStock(request);

            return response.getAvailable();

        } catch (Exception ex) {
            log.error("gRPC inventory call failed: {}", ex.getMessage());
            throw ex; // Re-throw so CircuitBreaker can track the failure
        }
    }

    // 🔥 fallback method
    public boolean fallback(String product, Throwable ex) {
        log.error("Inventory service failed, fallback triggered", ex);

        // ❗ Decide business behavior
        return false; // treat as out-of-stock
    }
}
