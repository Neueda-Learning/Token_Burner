package com.example.paymentprocessing.dto.response;

import com.example.paymentprocessing.enums.PaymentStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for a single payment history record.
 * <p>
 * History records use {@code changedAt} to represent the time of the status
 * transition. They should not reuse payment-level {@code updatedAt} semantics.
 * </p>
 */
public record PaymentHistoryResponse(
        Long historyId,
        PaymentStatus previousStatus,
        PaymentStatus newStatus,
        LocalDateTime changedAt,
        String notes
) {
}
