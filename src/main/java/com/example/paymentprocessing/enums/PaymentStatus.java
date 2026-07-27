package com.example.paymentprocessing.enums;

/**
 * Represents the fixed payment lifecycle for the training project.
 * <p>
 * These values describe how a payment moves through processing from creation
 * to either successful completion or failure.
 * </p>
 * <p>
 * {@code COMPLETED} and {@code FAILED} are terminal states.
 * The service layer is responsible for enforcing valid status transitions.
 * </p>
 */
public enum PaymentStatus {

    CREATED,

    VALIDATED,

    SENT,

    COMPLETED,

    FAILED

}
