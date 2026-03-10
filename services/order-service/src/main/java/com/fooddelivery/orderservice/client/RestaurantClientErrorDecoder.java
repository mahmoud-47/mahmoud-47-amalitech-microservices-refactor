package com.fooddelivery.orderservice.client;

import com.fooddelivery.orderservice.exception.RestaurantBadRequestException;
import com.fooddelivery.orderservice.exception.RestaurantNotFoundException;
import com.fooddelivery.orderservice.exception.RestaurantServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;

public class RestaurantClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        try {
            String body = response.body() != null
                    ? new String(response.body().asInputStream().readAllBytes())
                    : "";

            switch (response.status()) {
                case 404:
                    return new RestaurantNotFoundException("Restaurant not found: " + body);
                case 400:
                    return new RestaurantBadRequestException("Bad request: " + body);
                case 500:
                    return new RestaurantServiceException("Internal server error: " + body);
                default:
                    return defaultDecoder.decode(methodKey, response);
            }

        } catch (IOException e) {
            return new RuntimeException("Error decoding response", e);
        }
    }
}