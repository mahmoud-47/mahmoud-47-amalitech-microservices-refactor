package com.fooddelivery.commonutils.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared DTO returned by Restaurant Service internal endpoint.
 * Used by Order Service via Feign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedRestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private boolean active;
    private int estimatedDeliveryMinutes;
}