package com.fooddelivery.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Constants — shared with consumer (customer-service) ──────────────────
    public static final String HELLO_EXCHANGE    = "hello.exchange";
    public static final String HELLO_QUEUE       = "hello.queue";
    public static final String HELLO_ROUTING_KEY = "hello.event";
    public static final String HELLO_DLQ         = "hello.queue.dlq";

    // ── Dead Letter Queue ─────────────────────────────────────────────────────
    @Bean
    public Queue helloDeadLetterQueue() {
        return QueueBuilder.durable(HELLO_DLQ).build();
    }

    // ── Main queue — routes failed messages to DLQ ───────────────────────────
    @Bean
    public Queue helloQueue() {
        return QueueBuilder.durable(HELLO_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", HELLO_DLQ)
                .build();
    }

    // ── Topic exchange ────────────────────────────────────────────────────────
    @Bean
    public TopicExchange helloExchange() {
        return new TopicExchange(HELLO_EXCHANGE);
    }

    // ── Binding: exchange + routing key → queue ───────────────────────────────
    @Bean
    public Binding helloBinding(Queue helloQueue, TopicExchange helloExchange) {
        return BindingBuilder
                .bind(helloQueue)
                .to(helloExchange)
                .with(HELLO_ROUTING_KEY);
    }

    // ── JSON message converter — serializes POJOs to JSON automatically ───────
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── RabbitTemplate with JSON converter ───────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}