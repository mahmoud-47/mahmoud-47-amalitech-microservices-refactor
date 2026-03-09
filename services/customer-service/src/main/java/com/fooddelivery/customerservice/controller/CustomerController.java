package com.fooddelivery.customerservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import com.fooddelivery.customerservice.dto.*;
import com.fooddelivery.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ── Auth ─────────────────────────────────────────────────────────────────
    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Registered successfully",
                customerService.register(request)));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful",
                customerService.login(request)));
    }

    // ── Customer profile ─────────────────────────────────────────────────────
    @GetMapping("/api/customers/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getProfile(
            @RequestHeader("X-Authenticated-User-Id") String username) {
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched",
                customerService.getProfile(username)));
    }

    @PutMapping("/api/customers/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @RequestHeader("X-Authenticated-User-Id") String username,
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                customerService.updateProfile(username, request)));
    }

    // ── Internal endpoints — called by other services via Feign ─────────────
    // Prefixed with /internal/ to distinguish from public API
    @GetMapping("/internal/customers/{id}")
    public ResponseEntity<SharedCustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getSharedById(id));
    }

    @GetMapping("/internal/customers/by-username/{username}")
    public ResponseEntity<SharedCustomerResponse> getCustomerByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(customerService.getSharedByUsername(username));
    }
}