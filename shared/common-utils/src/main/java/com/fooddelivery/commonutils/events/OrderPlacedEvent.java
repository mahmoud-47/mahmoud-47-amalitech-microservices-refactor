package com.fooddelivery.commonutils.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String restaurantName;
    private String deliveryAddress;
    private String restaurantAddress;   // snapshot so Delivery Service needs no Feign call
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
}