package com.rdutta.inventoryservice.service;

import com.rdutta.inventoryservice.entity.Inventory;
import com.rdutta.inventoryservice.repository.InventoryRepository;
import io.micrometer.core.instrument.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository repository;
    private final MeterRegistry meterRegistry;

    public boolean isAvailable(String product) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            String normalizedProduct = product.trim();
            // ✅ Safely handle the Optional from MongoDB
            return repository.findByProduct(normalizedProduct)
                    .map(inventory -> {
                        log.info("Found inventory for product: {}", normalizedProduct);
                        boolean available = inventory.getQuantity() > 0;

                        // Increment counter for successful lookups
                        meterRegistry.counter("inventory.check", "product", normalizedProduct, "result", "found").increment();
                        return available;
                    })
                    .orElseGet(() -> {
                        // ❌ Product not in MongoDB
                        log.warn("Product '{}' not found in inventory collection", normalizedProduct);
                        meterRegistry.counter("inventory.check", "product", normalizedProduct, "result", "not_found").increment();
                        return false;
                    });

        } finally {
            sample.stop(Timer.builder("inventory.check.duration").register(meterRegistry));
        }
    }

    public Inventory create(Inventory inv) {
        return repository.save(inv);
    }
}