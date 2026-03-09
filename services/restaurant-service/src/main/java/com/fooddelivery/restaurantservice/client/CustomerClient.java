package com.fooddelivery.restaurantservice.client;

import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "customer-service",
        fallback = CustomerClientFallback.class
)
public interface CustomerClient {

    @GetMapping("/internal/customers/{id}")
    SharedCustomerResponse getById(@PathVariable Long id);

    @GetMapping("/internal/customers/by-username/{username}")
    SharedCustomerResponse getByUsername(@PathVariable String username);
}