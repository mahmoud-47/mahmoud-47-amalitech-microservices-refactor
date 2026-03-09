package com.fooddelivery.deliveryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Delivery Service owns:
 *  - delivery.exchange (publishes DeliveryStatusUpdatedEvent)
 *  - delivery.status.queue
 *
 * It CONSUMES from Order Service queues — those are declared by Order Service.
 * We declare bindings here so Delivery Service works independently if Order Service
 * restarts, but we do NOT redeclare the order exchange (Order Service owns it).
 */
@Configuration
public class RabbitMQConfig {

    // Queues declared by Order Service — we just reference them
    public static final String ORDER_PLACED_QUEUE    = "order.placed.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";

    // Delivery Service exchange and queue
    public static final String DELIVERY_EXCHANGE           = "delivery.exchange";
    public static final String DELIVERY_STATUS_ROUTING_KEY = "delivery.status.updated";
    public static final String DELIVERY_STATUS_QUEUE       = "delivery.status.queue";
    public static final String DELIVERY_STATUS_DLQ         = "delivery.status.queue.dlq";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    @Bean
    public Queue deliveryStatusDeadLetterQueue() {
        return QueueBuilder.durable(DELIVERY_STATUS_DLQ).build();
    }

    @Bean
    public Queue deliveryStatusQueue() {
        return QueueBuilder.durable(DELIVERY_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DELIVERY_STATUS_DLQ)
                .build();
    }

    @Bean
    public Binding deliveryStatusBinding() {
        return BindingBuilder.bind(deliveryStatusQueue())
                .to(deliveryExchange())
                .with(DELIVERY_STATUS_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}