package com.fooddelivery.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE            = "order.exchange";
    public static final String ORDER_PLACED_ROUTING_KEY  = "order.placed";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    public static final String ORDER_PLACED_QUEUE        = "order.placed.queue";
    public static final String ORDER_CANCELLED_QUEUE     = "order.cancelled.queue";
    public static final String ORDER_PLACED_DLQ          = "order.placed.queue.dlq";
    public static final String ORDER_CANCELLED_DLQ       = "order.cancelled.queue.dlq";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderPlacedDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_PLACED_DLQ).build();
    }

    @Bean
    public Queue orderCancelledDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_DLQ).build();
    }

    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ORDER_PLACED_DLQ)
                .build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ORDER_CANCELLED_DLQ)
                .build();
    }

    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder.bind(orderPlacedQueue())
                .to(orderExchange())
                .with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_ROUTING_KEY);
    }

    // ── Delivery Service queue — Order Service subscribes to this ────────────
    // Queue is declared by Delivery Service; we just reference it here.
    // No @Bean needed — just used as a constant in DeliveryEventConsumer.
    // If Delivery Service hasn't started yet, @RabbitListener will wait for it.

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