package com.fooddelivery.customerservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class HelloController {

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/hello-customer")
    public ResponseEntity<ApiResponse<Void>> hello(
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Authenticated-User-Role", required = false) String role
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Hello, I am service " + serviceName)
        );
    }
}