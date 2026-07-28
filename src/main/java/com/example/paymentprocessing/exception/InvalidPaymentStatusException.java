package com.example.paymentprocessing.exception;

import com.example.paymentprocessing.enums.PaymentStatus;

/**
 * Business exception thrown for invalid payment status changes.
 */
public class InvalidPaymentStatusException extends RuntimeException {

    public InvalidPaymentStatusException(PaymentStatus fromStatus, PaymentStatus toStatus) {
        super("Invalid payment status transition: %s -> %s.".formatted(fromStatus, toStatus));
    }

    public InvalidPaymentStatusException(String message) {
        super(message);
    }

    public InvalidPaymentStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}

