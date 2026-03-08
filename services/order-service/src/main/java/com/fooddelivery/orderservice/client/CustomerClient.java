package com.fooddelivery.orderservice.client;

import com.fooddelivery.commonutils.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "customer-service",
        fallback = CustomerClientFallback.class
)
@Component
public interface CustomerClient {

    @GetMapping("api/customers/hello-customer")
    ApiResponse<Void> hello(
            @RequestHeader("X-Authenticated-User-Id") String userId,
            @RequestHeader("X-Authenticated-User-Role") String role
    );
}