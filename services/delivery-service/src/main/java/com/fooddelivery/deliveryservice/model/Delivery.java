package com.fooddelivery.deliveryservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * All address data and IDs are populated from OrderPlacedEvent
 * at creation time — no cross-domain Feign calls needed at read time.
 *
 * restaurantName is snapshotted from the event for display purposes.
 */
@Entity
@Table(name = "deliveries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private String driverName;
    private String driverPhone;

    private String pickupAddress;
    private String deliveryAddress;

    // Snapshot from OrderPlacedEvent — avoids read-time Feign calls
    private Long customerId;
    private Long restaurantId;
    private String restaurantName;  // snapshotted at creation

    @Column(nullable = false)
    private Long orderId;

    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = DeliveryStatus.PENDING;
    }

    public enum DeliveryStatus {
        PENDING,
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        DELIVERED,
        FAILED
    }
}