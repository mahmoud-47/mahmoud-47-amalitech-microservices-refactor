package com.fooddelivery.commonutils.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusUpdatedEvent {
    private Long deliveryId;
    private Long orderId;
    private Long customerId;
    private String newStatus;
    private String driverName;
    private String driverPhone;
    private LocalDateTime updatedAt;
}