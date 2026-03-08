package com.fooddelivery.orderservice.client;

import com.fooddelivery.commonutils.dto.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback implements CustomerClient {

    @Override
    public ApiResponse<Void> hello(String userId, String role) {
        return ApiResponse.error("Customer service is currently unavailable");
    }
}