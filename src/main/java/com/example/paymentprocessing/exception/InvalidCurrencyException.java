package com.example.paymentprocessing.exception;

/**
 * Business exception thrown when a payment currency is null or unsupported.
 */
public class InvalidCurrencyException extends RuntimeException {

    public InvalidCurrencyException() {
        super("Payment currency is invalid or unsupported.");
    }

    public InvalidCurrencyException(String message) {
        super(message);
    }

    public InvalidCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}

