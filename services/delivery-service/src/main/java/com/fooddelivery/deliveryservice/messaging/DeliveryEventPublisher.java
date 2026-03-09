package com.fooddelivery.deliveryservice.messaging;

import com.fooddelivery.commonutils.events.DeliveryStatusUpdatedEvent;
import com.fooddelivery.deliveryservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public DeliveryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishStatusUpdated(DeliveryStatusUpdatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELIVERY_EXCHANGE,
                RabbitMQConfig.DELIVERY_STATUS_ROUTING_KEY,
                event
        );
        log.info("Published DeliveryStatusUpdatedEvent: orderId={}, status={}",
                event.getOrderId(), event.getNewStatus());
    }
}