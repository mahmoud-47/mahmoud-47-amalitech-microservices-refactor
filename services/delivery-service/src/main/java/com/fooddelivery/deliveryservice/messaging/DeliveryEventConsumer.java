package com.fooddelivery.deliveryservice.messaging;

import com.fooddelivery.commonutils.events.OrderCancelledEvent;
import com.fooddelivery.commonutils.events.OrderPlacedEvent;
import com.fooddelivery.deliveryservice.config.RabbitMQConfig;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryEventConsumer {

    private final DeliveryService deliveryService;

    public DeliveryEventConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Triggered when a new order is placed.
     * Creates and assigns delivery asynchronously.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for orderId: {}", event.getOrderId());
        try {
            deliveryService.handleOrderPlaced(event);
        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent for orderId: {}",
                    event.getOrderId(), e);
            throw e; // re-throw so RabbitMQ routes to DLQ after retries
        }
    }

    /**
     * Triggered when an order is cancelled by the customer.
     * Cancels the associated delivery.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent for orderId: {}", event.getOrderId());
        try {
            deliveryService.handleOrderCancelled(event);
        } catch (Exception e) {
            log.error("Failed to process OrderCancelledEvent for orderId: {}",
                    event.getOrderId(), e);
            throw e;
        }
    }
}