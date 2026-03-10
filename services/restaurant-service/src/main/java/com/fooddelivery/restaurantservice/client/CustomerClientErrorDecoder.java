package com.fooddelivery.restaurantservice.client;

import com.fooddelivery.restaurantservice.exception.BadRequestException;
import com.fooddelivery.restaurantservice.exception.CustomerNotFoundException;
import com.fooddelivery.restaurantservice.exception.CustomerServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;

public class CustomerClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String body = response.body() != null
                    ? new String(response.body().asInputStream().readAllBytes())
                    : "";

            switch (response.status()) {
                case 404:
                    return new CustomerNotFoundException("Customer not found: " + body);
                case 400:
                    return new BadRequestException("Bad request: " + body);
                case 500:
                    return new CustomerServiceException("Internal server error: " + body);
                default:
                    return defaultDecoder.decode(methodKey, response);
            }
        } catch (IOException e) {
            return new RuntimeException("Error decoding response", e);
        }
    }
}