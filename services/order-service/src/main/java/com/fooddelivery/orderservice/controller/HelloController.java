package com.fooddelivery.orderservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<Void>> hello() {
        return ResponseEntity.ok(
                ApiResponse.ok("Hello, I am service " + serviceName)
        );
    }
}