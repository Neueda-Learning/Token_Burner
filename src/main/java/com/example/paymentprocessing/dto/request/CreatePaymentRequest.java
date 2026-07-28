package com.example.paymentprocessing.dto.request;

import java.math.BigDecimal;

/**
 * Request DTO for creating a payment.
 * <p>
 * {@code paymentPassword} exists only for request-time verification. It must
 * never be stored in the database and must never appear in any response DTO.
 * </p>
 * <p>
 * TODO later: add validation for required account IDs, positive amount, and
 * supported currency format.
 * </p>
 */
public record CreatePaymentRequest(
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        String paymentPassword
) {
}
