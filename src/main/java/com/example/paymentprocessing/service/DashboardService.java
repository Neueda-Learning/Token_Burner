package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.response.DashboardSummaryResponse;

/**
 * Service contract for operational dashboard summary metrics.
 */
public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary();
}

