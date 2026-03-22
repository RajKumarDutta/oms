package com.rdutta.inventoryservice.config;

import com.rdutta.inventoryservice.grpc.InventoryGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcServerInterceptor;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServerConfig {

    @Bean
    public Server grpcServer(InventoryGrpcService service, ObservationRegistry observationRegistry) throws IOException {

        // 🛠️ The "Secret Sauce" for Trace IDs and Error Mapping
        ObservationGrpcServerInterceptor tracingInterceptor = new ObservationGrpcServerInterceptor(observationRegistry);
        ExceptionInterceptor exceptionInterceptor = new ExceptionInterceptor();

        Server server = ServerBuilder
                .forPort(6565)
                .intercept(tracingInterceptor)   // 👈 Fixes [traceId=, spanId=]
                .intercept(exceptionInterceptor) // 👈 Handles the MongoDB .get() crash
                .addService(service)
                .build()
                .start();

        log.info("🚀 gRPC Server started on port 6565 with Observability");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server...");
            server.shutdown();
        }));

        return server;
    }


}