package com.example.paymentprocessing.dto.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for operational dashboard summary metrics.
 */
public record DashboardSummaryResponse(
        long totalUsers,
        long totalPayments,
        BigDecimal totalTransactionAmount,
        long completedPayments,
        long failedPayments,
        Map<String, Long> statusDistribution
) {
}

