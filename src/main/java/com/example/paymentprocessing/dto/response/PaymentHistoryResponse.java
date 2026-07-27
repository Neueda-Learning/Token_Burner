package com.example.paymentprocessing.dto.response;

/**
 * TODO Leon:
 * Define the response DTO for returning payment status history.
 * Add response fields and nested history representation later.
 * Future response shape should align with history record semantics, such as:
 * - paymentId
 * - history[].historyId
 * - history[].previousStatus
 * - history[].newStatus
 * - history[].changedAt
 * - history[].notes
 * Do not use payment-level updatedAt for history entries.
 */
public class PaymentHistoryResponse {
    // TODO Add payment history response fields using changedAt-based history semantics.
}

