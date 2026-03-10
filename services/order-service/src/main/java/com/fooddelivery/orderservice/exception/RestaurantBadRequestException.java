package com.fooddelivery.orderservice.exception;

public class RestaurantBadRequestException extends RuntimeException {

    public RestaurantBadRequestException() { super(); }

    public RestaurantBadRequestException(String message) { super(message); }

    public RestaurantBadRequestException(String message, Throwable cause) { super(message, cause); }
}