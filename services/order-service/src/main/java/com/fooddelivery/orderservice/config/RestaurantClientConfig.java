package com.fooddelivery.orderservice.config;

import com.fooddelivery.orderservice.client.RestaurantClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestaurantClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new RestaurantClientErrorDecoder();
    }
}