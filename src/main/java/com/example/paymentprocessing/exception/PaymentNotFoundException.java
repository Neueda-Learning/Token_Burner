package com.example.paymentprocessing.exception;

/**
 * Business exception thrown when a payment cannot be found.
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long paymentId) {
        super("Payment with id %d was not found.".formatted(paymentId));
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

