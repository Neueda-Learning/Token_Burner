package com.example.paymentprocessing.dto.response;

import com.example.paymentprocessing.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for returning payment details.
 * <p>
 * This response must never expose a payment password or a password hash.
 * </p>
 */
public record PaymentResponse(
        Long paymentId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
