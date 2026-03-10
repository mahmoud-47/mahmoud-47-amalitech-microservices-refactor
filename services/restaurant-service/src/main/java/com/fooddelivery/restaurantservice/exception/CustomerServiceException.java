package com.fooddelivery.restaurantservice.exception;

public class CustomerServiceException extends RuntimeException {

    public CustomerServiceException() {
        super();
    }

    public CustomerServiceException(String message) {
        super(message);
    }

    public CustomerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}