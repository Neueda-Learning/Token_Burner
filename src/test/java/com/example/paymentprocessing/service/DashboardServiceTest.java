package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.response.DashboardSummaryResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardSummaryReturnsCorrectMetrics() {
        when(userRepository.count()).thenReturn(15L);
        when(paymentRepository.count()).thenReturn(42L);
        when(paymentRepository.sumAllAmounts()).thenReturn(new BigDecimal("5250.75"));
        when(paymentRepository.countByStatus(PaymentStatus.COMPLETED)).thenReturn(25L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(5L);

        PaymentRepository.PaymentStatusCountProjection created = new PaymentRepository.PaymentStatusCountProjection() {
            @Override
            public PaymentStatus getStatus() {
                return PaymentStatus.CREATED;
            }

            @Override
            public Long getTotal() {
                return 5L;
            }
        };

        PaymentRepository.PaymentStatusCountProjection validated = new PaymentRepository.PaymentStatusCountProjection() {
            @Override
            public PaymentStatus getStatus() {
                return PaymentStatus.VALIDATED;
            }

            @Override
            public Long getTotal() {
                return 7L;
            }
        };

        when(paymentRepository.countPaymentsGroupedByStatus()).thenReturn(List.of(created, validated));

        DashboardSummaryResponse response = dashboardService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(15L, response.totalUsers());
        assertEquals(42L, response.totalPayments());
        assertEquals(new BigDecimal("5250.75"), response.totalTransactionAmount());
        assertEquals(25L, response.completedPayments());
        assertEquals(5L, response.failedPayments());
        assertNotNull(response.statusDistribution());
    }

    @Test
    void getDashboardSummaryHandlesNullTotalAmount() {
        when(userRepository.count()).thenReturn(10L);
        when(paymentRepository.count()).thenReturn(20L);
        when(paymentRepository.sumAllAmounts()).thenReturn(null);
        when(paymentRepository.countByStatus(PaymentStatus.COMPLETED)).thenReturn(15L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(2L);

        PaymentRepository.PaymentStatusCountProjection created = new PaymentRepository.PaymentStatusCountProjection() {
            @Override
            public PaymentStatus getStatus() {
                return PaymentStatus.CREATED;
            }

            @Override
            public Long getTotal() {
                return 3L;
            }
        };

        when(paymentRepository.countPaymentsGroupedByStatus()).thenReturn(List.of(created));

        DashboardSummaryResponse response = dashboardService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.totalTransactionAmount());
    }

    @Test
    void getDashboardSummaryStatusDistributionIsInitialized() {
        when(userRepository.count()).thenReturn(5L);
        when(paymentRepository.count()).thenReturn(8L);
        when(paymentRepository.sumAllAmounts()).thenReturn(new BigDecimal("1000.00"));
        when(paymentRepository.countByStatus(PaymentStatus.COMPLETED)).thenReturn(3L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(1L);
        when(paymentRepository.countPaymentsGroupedByStatus()).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary();

        Map<String, Long> distribution = response.statusDistribution();
        assertNotNull(distribution);
        // All statuses should be initialized (even if to 0)
        for (PaymentStatus status : PaymentStatus.values()) {
            assertEquals(0L, distribution.getOrDefault(status.name(), -1L));
        }
    }

    @Test
    void getDashboardSummaryWithMultipleStatuses() {
        when(userRepository.count()).thenReturn(20L);
        when(paymentRepository.count()).thenReturn(50L);
        when(paymentRepository.sumAllAmounts()).thenReturn(new BigDecimal("10000.00"));
        when(paymentRepository.countByStatus(PaymentStatus.COMPLETED)).thenReturn(30L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(8L);
        when(paymentRepository.countPaymentsGroupedByStatus()).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(20L, response.totalUsers());
        assertEquals(50L, response.totalPayments());
        assertEquals(30L, response.completedPayments());
        assertEquals(8L, response.failedPayments());
    }
}



