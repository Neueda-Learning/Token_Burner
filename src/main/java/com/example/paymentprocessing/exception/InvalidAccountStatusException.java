package com.example.paymentprocessing.exception;

/**
 * Business exception thrown when an account is not in a valid status for payment processing.
 */
public class InvalidAccountStatusException extends RuntimeException {

    public InvalidAccountStatusException() {
        super("Account status does not allow this payment operation.");
    }

    public InvalidAccountStatusException(String message) {
        super(message);
    }

    public InvalidAccountStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}

