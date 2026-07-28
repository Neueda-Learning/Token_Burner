package com.example.paymentprocessing.exception;

/**
 * Business exception thrown when a user cannot be found.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User with id %d was not found.".formatted(userId));
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

