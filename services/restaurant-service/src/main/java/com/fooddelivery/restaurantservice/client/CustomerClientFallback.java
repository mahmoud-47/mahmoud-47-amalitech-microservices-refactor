package com.fooddelivery.restaurantservice.client;

import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback implements CustomerClient {

    @Override
    public SharedCustomerResponse getById(Long id) {
        throw new IllegalStateException("Customer Service unavailable — cannot validate customer");
    }

    @Override
    public SharedCustomerResponse getByUsername(String username) {
        throw new IllegalStateException("Customer Service unavailable — cannot validate owner");
    }
}