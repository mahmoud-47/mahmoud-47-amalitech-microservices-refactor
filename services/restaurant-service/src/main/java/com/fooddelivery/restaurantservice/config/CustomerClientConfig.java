package com.fooddelivery.restaurantservice.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomerClientErrorDecoder();
    }
}