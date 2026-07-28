package com.example.paymentprocessing.exception;

/**
 * Business exception thrown when a payment amount is invalid.
 */
public class InvalidPaymentAmountException extends RuntimeException {

    public InvalidPaymentAmountException() {
        super("Payment amount is invalid.");
    }

    public InvalidPaymentAmountException(String message) {
        super(message);
    }

    public InvalidPaymentAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}

