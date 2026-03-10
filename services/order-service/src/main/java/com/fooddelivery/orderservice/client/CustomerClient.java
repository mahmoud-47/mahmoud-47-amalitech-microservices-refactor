package com.fooddelivery.orderservice.client;

import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import com.fooddelivery.orderservice.config.CustomerClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", fallback = CustomerClientFallback.class, configuration = CustomerClientConfig.class)
public interface CustomerClient {

    @GetMapping("/internal/customers/{id}")
    SharedCustomerResponse getById(@PathVariable Long id);

    @GetMapping("/internal/customers/by-username/{username}")
    SharedCustomerResponse getByUsername(@PathVariable String username);
}