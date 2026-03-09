package com.fooddelivery.apigateway.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Called by Spring Cloud Gateway CircuitBreaker filter when a downstream
 * service is open (unavailable). Returns a clean 503 instead of a timeout.
 */
@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @RequestMapping("/customer-service")
    public ResponseEntity<ApiResponse<Void>> customerFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Customer service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/restaurant-service")
    public ResponseEntity<ApiResponse<Void>> restaurantFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Restaurant service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/order-service")
    public ResponseEntity<ApiResponse<Void>> orderFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Order service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/delivery-service")
    public ResponseEntity<ApiResponse<Void>> deliveryFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Delivery service is temporarily unavailable. Please try again later."));
    }
}