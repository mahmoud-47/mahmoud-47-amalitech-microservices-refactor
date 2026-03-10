package com.fooddelivery.orderservice.config;

import com.fooddelivery.orderservice.client.CustomerClientErrorDecoder;
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