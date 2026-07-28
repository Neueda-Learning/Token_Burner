package com.example.paymentprocessing.exception;

/**
 * Business exception thrown when payment password verification fails.
 */
public class InvalidPaymentPasswordException extends RuntimeException {

    public InvalidPaymentPasswordException() {
        super("Payment password verification failed.");
    }

    public InvalidPaymentPasswordException(String message) {
        super(message);
    }

    public InvalidPaymentPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}

