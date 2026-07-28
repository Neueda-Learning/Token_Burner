package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.response.DashboardSummaryResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Real, database-backed implementation for dashboard summary metrics.
 */
@Service
@Profile("!mock")
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public DashboardServiceImpl(UserRepository userRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long totalUsers = userRepository.count();
        long totalPayments = paymentRepository.count();
        BigDecimal totalTransactionAmount = paymentRepository.sumAllAmounts();
        if (totalTransactionAmount == null) {
            totalTransactionAmount = BigDecimal.ZERO;
        }
        long completedPayments = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (PaymentStatus status : PaymentStatus.values()) {
            statusDistribution.put(status.name(), 0L);
        }

        for (PaymentRepository.PaymentStatusCountProjection row : paymentRepository.countPaymentsGroupedByStatus()) {
            statusDistribution.put(row.getStatus().name(), row.getTotal());
        }

        return new DashboardSummaryResponse(
                totalUsers,
                totalPayments,
                totalTransactionAmount,
                completedPayments,
                failedPayments,
                statusDistribution
        );
    }
}

