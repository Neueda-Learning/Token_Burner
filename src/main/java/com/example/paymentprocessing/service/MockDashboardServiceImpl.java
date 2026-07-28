package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.response.DashboardSummaryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mock dashboard summary implementation used under the "mock" Spring profile.
 */
@Service
@Profile("mock")
public class MockDashboardServiceImpl implements DashboardService {

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("CREATED", 0L);
        statusDistribution.put("VALIDATED", 0L);
        statusDistribution.put("SENT", 1L);
        statusDistribution.put("COMPLETED", 1L);
        statusDistribution.put("FAILED", 0L);

        return new DashboardSummaryResponse(
                3L,
                2L,
                new BigDecimal("1775.50"),
                1L,
                0L,
                statusDistribution
        );
    }
}

