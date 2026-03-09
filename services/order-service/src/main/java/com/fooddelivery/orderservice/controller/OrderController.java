package com.fooddelivery.orderservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import com.fooddelivery.commonutils.dto.SharedOrderResponse;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.PlaceOrderRequest;
import com.fooddelivery.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ── Customer endpoints ────────────────────────────────────────────────────
    @PostMapping("/api/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestHeader("X-Authenticated-User-Id") String username,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order placed",
                orderService.placeOrder(username, request)));
    }

    @GetMapping("/api/orders/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestHeader("X-Authenticated-User-Id") String username) {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched",
                orderService.getCustomerOrders(username)));
    }

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Order fetched",
                orderService.getById(id)));
    }

    @PatchMapping("/api/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable Long id,
            @RequestHeader("X-Authenticated-User-Id") String username) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled",
                orderService.cancelOrder(id, username)));
    }

    // ── Restaurant-facing ─────────────────────────────────────────────────────
    @GetMapping("/api/orders/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRestaurantOrders(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched",
                orderService.getRestaurantOrders(restaurantId)));
    }

    @PatchMapping("/api/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                orderService.updateStatus(id, status)));
    }

    // ── Internal endpoint — called by Delivery Service via Feign ─────────────
    @GetMapping("/internal/orders/{id}")
    public ResponseEntity<SharedOrderResponse> getSharedOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getSharedById(id));
    }
}