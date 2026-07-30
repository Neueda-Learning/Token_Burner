package com.example.paymentprocessing.controller;

import com.example.paymentprocessing.dto.response.DashboardSummaryResponse;
import com.example.paymentprocessing.exception.GlobalExceptionHandler;
import com.example.paymentprocessing.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getDashboardSummaryReturnsOkWithResponse() throws Exception {
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("CREATED", 5L);
        statusDistribution.put("VALIDATED", 7L);
        statusDistribution.put("SENT", 3L);
        statusDistribution.put("COMPLETED", 25L);
        statusDistribution.put("FAILED", 5L);

        DashboardSummaryResponse response = new DashboardSummaryResponse(
                15L,
                45L,
                new BigDecimal("5250.75"),
                25L,
                5L,
                statusDistribution
        );

        when(dashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(15))
                .andExpect(jsonPath("$.totalPayments").value(45))
                .andExpect(jsonPath("$.totalTransactionAmount").value("5250.75"))
                .andExpect(jsonPath("$.completedPayments").value(25))
                .andExpect(jsonPath("$.failedPayments").value(5));
    }

    @Test
    void getDashboardSummaryReturnsStatusDistribution() throws Exception {
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("CREATED", 2L);
        statusDistribution.put("VALIDATED", 3L);
        statusDistribution.put("SENT", 1L);
        statusDistribution.put("COMPLETED", 10L);
        statusDistribution.put("FAILED", 2L);

        DashboardSummaryResponse response = new DashboardSummaryResponse(
                10L,
                18L,
                new BigDecimal("2000.00"),
                10L,
                2L,
                statusDistribution
        );

        when(dashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDistribution", hasEntry("CREATED", 2)))
                .andExpect(jsonPath("$.statusDistribution", hasEntry("VALIDATED", 3)))
                .andExpect(jsonPath("$.statusDistribution", hasEntry("COMPLETED", 10)))
                .andExpect(jsonPath("$.statusDistribution", hasEntry("FAILED", 2)));
    }

    @Test
    void getDashboardSummaryWithZeroPayments() throws Exception {
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("CREATED", 0L);
        statusDistribution.put("VALIDATED", 0L);
        statusDistribution.put("SENT", 0L);
        statusDistribution.put("COMPLETED", 0L);
        statusDistribution.put("FAILED", 0L);

        DashboardSummaryResponse response = new DashboardSummaryResponse(
                5L,
                0L,
                BigDecimal.ZERO,
                0L,
                0L,
                statusDistribution
        );

        when(dashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayments").value(0))
                .andExpect(jsonPath("$.totalTransactionAmount").value("0"));
    }
}


