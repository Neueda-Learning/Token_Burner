package com.example.paymentprocessing.controller;

//todo Kylian

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.exception.GlobalExceptionHandler;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.exception.InvalidPaymentStatusException;
import com.example.paymentprocessing.exception.PaymentNotFoundException;
import com.example.paymentprocessing.exception.UserNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
        // 这个用例验证按支付单号查询成功时，接口会返回 200 和完整的支付详情 JSON。
    void getPaymentByIdReturnsOkAndPaymentBody() throws Exception {
        PaymentResponse response = buildPaymentResponse(101L, 1L, 2L, "150.00", "USD", PaymentStatus.CREATED);
        when(paymentService.getPaymentById(101L)).thenReturn(response);

        mockMvc.perform(get("/api/payments/{paymentId}", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(101))
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.destinationAccountId").value(2))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
        // 这个用例验证查询不存在的支付单时，接口会被全局异常处理器转换为 404 错误响应。
    void getPaymentByIdNotFoundReturns404ErrorBody() throws Exception {
        when(paymentService.getPaymentById(999L)).thenThrow(new PaymentNotFoundException(999L));

        mockMvc.perform(get("/api/payments/{paymentId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment with id 999 was not found."))
                .andExpect(jsonPath("$.path").value("/api/payments/999"));
    }

    @Test
        // 这个用例验证创建支付成功时，接口会返回 201 并输出服务层返回的支付信息。
    void createPaymentReturnsCreatedAndPaymentBody() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("88.50"), "USD", "123456");
        PaymentResponse response = buildPaymentResponse(202L, 1L, 2L, "88.50", "USD", PaymentStatus.CREATED);
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(202))
                .andExpect(jsonPath("$.amount").value(88.50))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(paymentService).createPayment(eq(request));
    }

    @Test
        // 这个用例验证创建支付参数中的币种非法时，接口会返回 400 与 INVALID_CURRENCY 错误码。
    void createPaymentInvalidCurrencyReturns400ErrorBody() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("88.50"), "ABC", "123456");
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenThrow(new InvalidCurrencyException());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURRENCY"))
                .andExpect(jsonPath("$.path").value("/api/payments"));
    }

    @Test
        // 这个用例验证按支付单号查询历史成功时，接口会返回 200 且 JSON 数组结构与顺序正确。
    void getPaymentHistoryReturnsOkAndHistoryList() throws Exception {
        LocalDateTime changedAt = LocalDateTime.of(2026, 7, 30, 12, 0, 0);
        PaymentHistoryResponse first = new PaymentHistoryResponse(1L, 333L, PaymentStatus.CREATED, PaymentStatus.VALIDATED, changedAt, "validated");
        PaymentHistoryResponse second = new PaymentHistoryResponse(2L, 333L, PaymentStatus.VALIDATED, PaymentStatus.SENT, changedAt.plusMinutes(1), "sent");
        when(paymentService.getPaymentHistory(333L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/payments/{paymentId}/history", 333L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].historyId").value(1))
                .andExpect(jsonPath("$[0].newStatus").value("VALIDATED"))
                .andExpect(jsonPath("$[1].historyId").value(2))
                .andExpect(jsonPath("$[1].newStatus").value("SENT"));
    }

    @Test
        // 这个用例验证查询用户支付记录成功时，接口会返回 200 并输出该用户相关的支付列表。
    void getPaymentsByUserReturnsOkAndPaymentList() throws Exception {
        PaymentResponse first = buildPaymentResponse(401L, 10L, 20L, "10.00", "USD", PaymentStatus.CREATED);
        PaymentResponse second = buildPaymentResponse(402L, 11L, 10L, "20.00", "EUR", PaymentStatus.COMPLETED);
        when(paymentService.getPaymentsByUser(10L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/payments/user/{userId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(401))
                .andExpect(jsonPath("$[1].paymentId").value(402));
    }

    @Test
        // 这个用例验证查询不存在用户的支付记录时，接口会返回 404 与 USER_NOT_FOUND 错误码。
    void getPaymentsByUserUserNotFoundReturns404ErrorBody() throws Exception {
        when(paymentService.getPaymentsByUser(404L)).thenThrow(new UserNotFoundException(404L));

        mockMvc.perform(get("/api/payments/user/{userId}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User with id 404 was not found."))
                .andExpect(jsonPath("$.path").value("/api/payments/user/404"));
    }

    @Test
        // 这个用例验证更新支付状态成功时，接口会返回 200 且状态字段更新为目标状态。
    void updatePaymentStatusReturnsOkAndUpdatedBody() throws Exception {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(PaymentStatus.VALIDATED);
        PaymentResponse response = buildPaymentResponse(501L, 1L, 2L, "66.00", "USD", PaymentStatus.VALIDATED);
        when(paymentService.updatePaymentStatus(eq(501L), any(UpdatePaymentStatusRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/payments/{paymentId}/status", 501L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(501))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
        // 这个用例验证更新支付状态发生非法流转时，接口会返回 400 与 INVALID_PAYMENT_STATUS 错误码。
    void updatePaymentStatusInvalidTransitionReturns400ErrorBody() throws Exception {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(PaymentStatus.COMPLETED);
        when(paymentService.updatePaymentStatus(eq(777L), any(UpdatePaymentStatusRequest.class)))
                .thenThrow(new InvalidPaymentStatusException("Cannot change payment status from CREATED to COMPLETED"));

        mockMvc.perform(put("/api/payments/{paymentId}/status", 777L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAYMENT_STATUS"))
                .andExpect(jsonPath("$.message").value("Cannot change payment status from CREATED to COMPLETED"))
                .andExpect(jsonPath("$.path").value("/api/payments/777/status"));
    }

    // 这个辅助函数用于快速构造支付响应对象，减少各接口场景中的重复测试数据拼装。
    private PaymentResponse buildPaymentResponse(
            Long paymentId,
            Long sourceAccountId,
            Long destinationAccountId,
            String amount,
            String currency,
            PaymentStatus status
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 30, 12, 0, 0);
        return new PaymentResponse(
                paymentId,
                sourceAccountId,
                destinationAccountId,
                new BigDecimal(amount),
                currency,
                status,
                now,
                now
        );
    }
}
