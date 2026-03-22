package com.rdutta.inventoryservice.grpc;

import com.grpc.inventory.*;

import com.rdutta.inventoryservice.service.InventoryService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryService service;

    @Override
    public void checkStock(InventoryRequest request,
                           StreamObserver<InventoryResponse> responseObserver) {
        log.info("Received gRPC request for product={}", request.getProduct());
        boolean available = service.isAvailable(request.getProduct());

        InventoryResponse response = InventoryResponse.newBuilder()
                .setAvailable(available)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}