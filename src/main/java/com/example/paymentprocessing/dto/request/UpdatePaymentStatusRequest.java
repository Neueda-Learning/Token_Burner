package com.example.paymentprocessing.dto.request;

import com.example.paymentprocessing.enums.PaymentStatus;

/**
 * Request DTO for updating a payment lifecycle status.
 * <p>
 * TODO later: add validation to ensure a status value is provided and that
 * transition rules are enforced in the service layer.
 * </p>
 */
public record UpdatePaymentStatusRequest(
        PaymentStatus status
) {
}
