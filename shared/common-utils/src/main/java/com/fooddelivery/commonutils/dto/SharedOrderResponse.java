package com.fooddelivery.commonutils.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared DTO returned by Order Service internal endpoint.
 * Used by Delivery Service via Feign to fetch order details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedOrderResponse {
    private Long id;
    private String status;
    private Long customerId;
    private Long restaurantId;
    private String deliveryAddress;
    private String restaurantAddress;   // snapshotted at order time
}