package com.rdutta.inventoryservice;

import com.rdutta.inventoryservice.entity.Inventory;
import com.rdutta.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryServiceApplicationTests {

    @Autowired
    private com.rdutta.inventoryservice.repository.InventoryRepository repository;

    @Test
    void testFindIphone() {
        System.out.println("====== DB TEST ======");
        java.util.Optional<com.rdutta.inventoryservice.entity.Inventory> result = repository.findByProduct("iphone-14");
        System.out.println("FOUND IPHONE 14: " + result.orElse(null));
        System.out.println("=====================");
        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent(), "iphone-14 should be found in inventorydb");
    }
}
