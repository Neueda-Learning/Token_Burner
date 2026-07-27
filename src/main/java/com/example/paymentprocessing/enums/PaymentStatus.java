package com.example.paymentprocessing.enums;

/**
 * TODO Kylian:
 * Define the supported payment lifecycle statuses.
 * The status set is now frozen for the current training scope.
 * Future service logic should enforce the agreed transitions documented in the API design.
 */
public enum PaymentStatus {

    CREATED,

    VALIDATED,

    SENT,

    COMPLETED,

    FAILED

}

