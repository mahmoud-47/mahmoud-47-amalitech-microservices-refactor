package com.fooddelivery.orderservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import com.fooddelivery.orderservice.client.CustomerClient;
import com.fooddelivery.orderservice.messaging.HelloEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CustomerClient customerClient;
    private final HelloEventPublisher helloEventPublisher;

    public OrderController(CustomerClient customerClient,
                           HelloEventPublisher helloEventPublisher) {
        this.customerClient = customerClient;
        this.helloEventPublisher = helloEventPublisher;
    }

    // Smoke test — calls customer-service /hello via Feign, forwarding identity
    @GetMapping("/hello-customer")
    public ResponseEntity<ApiResponse<Void>> helloCustomer(
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Authenticated-User-Role", required = false) String role) {

        ApiResponse<Void> response = customerClient.hello(userId, role);
        return ResponseEntity.ok(response);
    }

    // Publishes a HelloEvent to RabbitMQ — customer-service will consume it
    @PostMapping("/say-hello")
    public ResponseEntity<ApiResponse<Void>> sayHello(
            @RequestHeader("X-Authenticated-User-Id") String userId) {

        helloEventPublisher.publishHello(userId);
        return ResponseEntity.ok(
                ApiResponse.ok("HelloEvent published for user: " + userId)
        );
    }
}