package com.fooddelivery.orderservice.exception;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException() { super(); }

    public RestaurantNotFoundException(String message) { super(message); }

    public RestaurantNotFoundException(String message, Throwable cause) { super(message, cause); }
}