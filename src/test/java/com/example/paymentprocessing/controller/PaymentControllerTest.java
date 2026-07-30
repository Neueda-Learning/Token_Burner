package com.example.paymentprocessing.controller;


//todo Kylian


import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.exception.GlobalExceptionHandler;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.exception.PaymentNotFoundException;
import com.example.paymentprocessing.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
        // 测试按支付单号查询时接口会返回 200 和正确的支付详情字段。
    void getPaymentByIdReturnsOkAndResponseBody() throws Exception {
        PaymentResponse response = buildPaymentResponse(101L, 1L, 2L, new BigDecimal("188.00"), "USD", PaymentStatus.SENT);
        when(paymentService.getPaymentById(101L)).thenReturn(response);

        mockMvc.perform(get("/api/payments/{paymentId}", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(101))
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.destinationAccountId").value(2))
                .andExpect(jsonPath("$.amount").value(188.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
        // 测试查询支付历史时接口会返回 200 并按数组结构返回历史记录。
    void getPaymentHistoryReturnsOkAndHistoryList() throws Exception {
        PaymentHistoryResponse first = new PaymentHistoryResponse(
                1L,
                200L,
                PaymentStatus.CREATED,
                PaymentStatus.VALIDATED,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "validated"
        );
        PaymentHistoryResponse second = new PaymentHistoryResponse(
                2L,
                200L,
                PaymentStatus.VALIDATED,
                PaymentStatus.SENT,
                LocalDateTime.of(2026, 1, 1, 10, 5),
                "sent"
        );
        when(paymentService.getPaymentHistory(200L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/payments/{paymentId}/history", 200L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].historyId").value(1))
                .andExpect(jsonPath("$[0].newStatus").value("VALIDATED"))
                .andExpect(jsonPath("$[1].historyId").value(2))
                .andExpect(jsonPath("$[1].newStatus").value("SENT"));
    }

    @Test
        // 测试按用户查询支付列表时接口会返回 200 并返回该用户相关支付数据。
    void getPaymentsByUserReturnsOkAndPaymentList() throws Exception {
        PaymentResponse first = buildPaymentResponse(301L, 10L, 20L, new BigDecimal("50.00"), "USD", PaymentStatus.CREATED);
        PaymentResponse second = buildPaymentResponse(302L, 99L, 10L, new BigDecimal("70.00"), "EUR", PaymentStatus.COMPLETED);
        when(paymentService.getPaymentsByUser(10L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/payments/user/{userId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(301))
                .andExpect(jsonPath("$[1].paymentId").value(302));
    }

    @Test
        // 测试创建支付成功时接口会返回 201 并返回创建后的支付详情。
    void createPaymentReturnsCreatedAndResponseBody() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        PaymentResponse response = buildPaymentResponse(501L, 1L, 2L, new BigDecimal("150.00"), "USD", PaymentStatus.CREATED);
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(501))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
        // 测试更新支付状态成功时接口会返回 200 并返回更新后的状态。
    void updatePaymentStatusReturnsOkAndUpdatedStatus() throws Exception {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(PaymentStatus.VALIDATED);
        PaymentResponse response = buildPaymentResponse(500L, 1L, 2L, new BigDecimal("60.00"), "USD", PaymentStatus.VALIDATED);
        when(paymentService.updatePaymentStatus(eq(500L), any(UpdatePaymentStatusRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/payments/{paymentId}/status", 500L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(500))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
        // 测试支付不存在异常时会被全局异常处理器映射为 404 错误响应。
    void getPaymentByIdReturnsNotFoundWhenPaymentDoesNotExist() throws Exception {
        when(paymentService.getPaymentById(999L)).thenThrow(new PaymentNotFoundException(999L));

        mockMvc.perform(get("/api/payments/{paymentId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/payments/999"));
    }

    @Test
        // 测试业务参数错误异常时会被全局异常处理器映射为 400 错误响应。
    void createPaymentReturnsBadRequestWhenCurrencyIsInvalid() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "ABC", "123456");
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenThrow(new InvalidCurrencyException());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURRENCY"))
                .andExpect(jsonPath("$.path").value("/api/payments"));
    }

    // 构造统一的支付响应对象，减少每个控制器测试中的重复样板。
    private PaymentResponse buildPaymentResponse(
            Long paymentId,
            Long sourceAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            String currency,
            PaymentStatus status
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        return new PaymentResponse(
                paymentId,
                sourceAccountId,
                destinationAccountId,
                amount,
                currency,
                status,
                now,
                now
        );
    }
}

