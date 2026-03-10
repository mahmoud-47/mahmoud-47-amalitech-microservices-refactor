package com.fooddelivery.orderservice.client;

import com.fooddelivery.commonutils.dto.SharedMenuItemResponse;
import com.fooddelivery.commonutils.dto.SharedRestaurantResponse;
import com.fooddelivery.orderservice.config.RestaurantClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service", fallback = RestaurantClientFallback.class, configuration = RestaurantClientConfig.class)
public interface RestaurantClient {

    @GetMapping("/internal/restaurants/{id}")
    SharedRestaurantResponse getById(@PathVariable Long id);

    @GetMapping("/internal/menu-items/{id}")
    SharedMenuItemResponse getMenuItem(@PathVariable Long id);
}