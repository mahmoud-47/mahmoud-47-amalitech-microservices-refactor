package com.fooddelivery.orderservice.client;

import com.fooddelivery.commonutils.dto.SharedMenuItemResponse;
import com.fooddelivery.commonutils.dto.SharedRestaurantResponse;
import org.springframework.stereotype.Component;

@Component
public class RestaurantClientFallback implements RestaurantClient {

    @Override
    public SharedRestaurantResponse getById(Long id) {
        throw new IllegalStateException("Restaurant Service unavailable");
    }

    @Override
    public SharedMenuItemResponse getMenuItem(Long id) {
        throw new IllegalStateException("Restaurant Service unavailable");
    }
}