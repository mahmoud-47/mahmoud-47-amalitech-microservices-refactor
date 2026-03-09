package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.commonutils.events.DeliveryStatusUpdatedEvent;
import com.fooddelivery.commonutils.events.OrderPlacedEvent;
import com.fooddelivery.commonutils.events.OrderCancelledEvent;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.messaging.DeliveryEventPublisher;
import com.fooddelivery.deliveryservice.model.Delivery;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventPublisher eventPublisher;

    // Simulated driver pool — in a real system this would come from a driver assignment service
    private static final String[] DRIVERS = {
            "Carlos Martinez", "Sarah Johnson", "Mike Chen", "Priya Patel", "James Wilson"
    };
    private static final String[] PHONES = {
            "+1-555-0101", "+1-555-0102", "+1-555-0103", "+1-555-0104", "+1-555-0105"
    };

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DeliveryEventPublisher eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Called by RabbitMQ consumer when OrderPlacedEvent arrives.
     * All data comes from the event — no Feign calls needed.
     * Addresses and IDs were snapshotted by Order Service in the event payload.
     */
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Idempotency guard — don't create duplicate deliveries
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Delivery already exists for orderId: {}, skipping", event.getOrderId());
            return;
        }

        int driverIndex = (int) (Math.random() * DRIVERS.length);

        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .restaurantId(event.getRestaurantId())
                .restaurantName(event.getRestaurantName())   // snapshot from event
                .pickupAddress(event.getRestaurantAddress()) // snapshot from event
                .deliveryAddress(event.getDeliveryAddress()) // snapshot from event
                .status(Delivery.DeliveryStatus.ASSIGNED)
                .driverName(DRIVERS[driverIndex])
                .driverPhone(PHONES[driverIndex])
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        log.info("Delivery #{} created for orderId: {}, assigned to {}",
                saved.getId(), event.getOrderId(), DRIVERS[driverIndex]);

        // Publish async notification back — Order Service or Customer Service can listen
        eventPublisher.publishStatusUpdated(DeliveryStatusUpdatedEvent.builder()
                .deliveryId(saved.getId())
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .newStatus(Delivery.DeliveryStatus.ASSIGNED.name())
                .driverName(DRIVERS[driverIndex])
                .driverPhone(PHONES[driverIndex])
                .updatedAt(LocalDateTime.now())
                .build());
    }

    /**
     * Called by RabbitMQ consumer when OrderCancelledEvent arrives.
     */
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        deliveryRepository.findByOrderId(event.getOrderId()).ifPresent(delivery -> {
            if (delivery.getStatus() == Delivery.DeliveryStatus.DELIVERED) {
                log.warn("Cannot cancel already-delivered delivery for orderId: {}",
                        event.getOrderId());
                return;
            }
            delivery.setStatus(Delivery.DeliveryStatus.FAILED);
            deliveryRepository.save(delivery);
            log.info("Delivery cancelled for orderId: {}", event.getOrderId());
        });
    }

    /**
     * Manual status update — called from the REST API (admin / driver).
     * Publishes DeliveryStatusUpdatedEvent so Order Service can sync its status.
     */
    @Transactional
    public DeliveryResponse updateStatus(Long deliveryId, String status) {
        Delivery delivery = findById(deliveryId);
        Delivery.DeliveryStatus newStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);

        switch (newStatus) {
            case PICKED_UP -> delivery.setPickedUpAt(LocalDateTime.now());
            case DELIVERED -> delivery.setDeliveredAt(LocalDateTime.now());
            default -> {}
        }

        Delivery saved = deliveryRepository.save(delivery);

        // ASYNC: Order Service subscribes to this and updates its own order status
        // No direct Feign call to Order Service needed
        eventPublisher.publishStatusUpdated(DeliveryStatusUpdatedEvent.builder()
                .deliveryId(saved.getId())
                .orderId(saved.getOrderId())
                .customerId(saved.getCustomerId())
                .newStatus(newStatus.name())
                .driverName(saved.getDriverName())
                .driverPhone(saved.getDriverPhone())
                .updatedAt(LocalDateTime.now())
                .build());

        return DeliveryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByOrderId(Long orderId) {
        return DeliveryResponse.fromEntity(
                deliveryRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Delivery not found for orderId: " + orderId)));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getById(Long id) {
        return DeliveryResponse.fromEntity(findById(id));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByStatus(String status) {
        return deliveryRepository
                .findByStatus(Delivery.DeliveryStatus.valueOf(status.toUpperCase()))
                .stream().map(DeliveryResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByCustomerId(Long customerId) {
        return deliveryRepository.findByCustomerId(customerId)
                .stream().map(DeliveryResponse::fromEntity).toList();
    }

    private Delivery findById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));
    }
}