package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Delivery fetched",
                deliveryService.getByOrderId(orderId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Delivery fetched",
                deliveryService.getById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.ok("Deliveries fetched",
                deliveryService.getByStatus(status)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Deliveries fetched",
                deliveryService.getByCustomerId(customerId)));
    }

    /**
     * Driver / admin updates delivery status.
     * Publishes DeliveryStatusUpdatedEvent → Order Service updates order status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                deliveryService.updateStatus(id, status)));
    }
}