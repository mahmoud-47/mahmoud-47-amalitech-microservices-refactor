package com.fooddelivery.orderservice.messaging;

import com.fooddelivery.commonutils.events.HelloEvent;
import com.fooddelivery.orderservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public HelloEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishHello(String userId) {
        HelloEvent event = HelloEvent.of(userId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.HELLO_EXCHANGE,
                RabbitMQConfig.HELLO_ROUTING_KEY,
                event
        );

        log.info("Published HelloEvent for user: {}", userId);
    }
}