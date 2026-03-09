package com.fooddelivery.orderservice.messaging;

import com.fooddelivery.commonutils.events.OrderCancelledEvent;
import com.fooddelivery.commonutils.events.OrderPlacedEvent;
import com.fooddelivery.orderservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                event
        );
        log.info("Published OrderPlacedEvent for orderId: {}", event.getOrderId());
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY,
                event
        );
        log.info("Published OrderCancelledEvent for orderId: {}", event.getOrderId());
    }
}