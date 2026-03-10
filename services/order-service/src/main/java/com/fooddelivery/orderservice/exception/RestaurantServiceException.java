package com.fooddelivery.orderservice.exception;

public class RestaurantServiceException extends RuntimeException {

    public RestaurantServiceException() { super(); }

    public RestaurantServiceException(String message) { super(message); }

    public RestaurantServiceException(String message, Throwable cause) { super(message, cause); }
}