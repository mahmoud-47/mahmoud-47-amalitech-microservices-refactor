package com.fooddelivery.commonutils.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Shared DTO returned by Restaurant Service internal endpoint.
 * Used by Order Service via Feign to validate menu items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedMenuItemResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean available;
    private Long restaurantId;
}