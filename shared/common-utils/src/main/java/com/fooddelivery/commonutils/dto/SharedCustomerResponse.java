package com.fooddelivery.commonutils.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared DTO returned by Customer Service internal endpoint.
 * Used by Restaurant Service and Order Service via Feign.
 * Deliberately minimal — only what other services need.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedCustomerResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String deliveryAddress;
    private String city;
    private String role;
}