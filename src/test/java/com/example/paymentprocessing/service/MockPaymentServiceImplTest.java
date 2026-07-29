package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.exception.InvalidPaymentPasswordException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MockPaymentServiceImplTest {

    @Test
    void activeAccountsWithinLimitRemainCreatedAfterCreation() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), "EUR", "888888");

        PaymentResponse response = mockPaymentService.createPayment(request);

        assertEquals("EUR", response.currency());
        assertEquals(PaymentStatus.CREATED, response.status());
    }

    @Test
    void inactiveSourceAccountCreatesFailedPaymentWithHistory() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(99L, 1002L, new BigDecimal("250.00"), "USD", "888888");

        PaymentResponse response = mockPaymentService.createPayment(request);
        List<PaymentHistoryResponse> history = mockPaymentService.getPaymentHistory(response.paymentId());

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals(2, history.size());
        assertEquals(PaymentStatus.CREATED, history.get(0).newStatus());
        assertEquals(PaymentStatus.CREATED, history.get(1).previousStatus());
        assertEquals(PaymentStatus.FAILED, history.get(1).newStatus());
        assertEquals("Payment failed: source or destination account is inactive", history.get(1).notes());
    }

    @Test
    void inactiveDestinationAccountCreatesFailedPaymentWithHistory() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 99L, new BigDecimal("250.00"), "USD", "888888");

        PaymentResponse response = mockPaymentService.createPayment(request);
        List<PaymentHistoryResponse> history = mockPaymentService.getPaymentHistory(response.paymentId());

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals(2, history.size());
        assertEquals("Payment failed: source or destination account is inactive", history.get(1).notes());
    }

    @Test
    void amountExactlySinglePaymentLimitIsAllowed() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("1000000.00"), "USD", "888888");

        PaymentResponse response = mockPaymentService.createPayment(request);

        assertEquals(PaymentStatus.CREATED, response.status());
    }

    @Test
    void amountAboveSinglePaymentLimitCreatesFailedPaymentWithHistory() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("1000000.01"), "USD", "888888");

        PaymentResponse response = mockPaymentService.createPayment(request);
        List<PaymentHistoryResponse> history = mockPaymentService.getPaymentHistory(response.paymentId());

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals(2, history.size());
        assertEquals(PaymentStatus.FAILED, history.get(1).newStatus());
        assertEquals("Payment failed: transaction amount exceeds the single-payment limit", history.get(1).notes());
    }

    @Test
    void unsupportedCurrencyThrowsInvalidCurrencyException() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), "ABC", "888888");

        assertThrows(InvalidCurrencyException.class, () -> mockPaymentService.createPayment(request));
    }

    @Test
    void nullCurrencyThrowsInvalidCurrencyException() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), null, "888888");

        assertThrows(InvalidCurrencyException.class, () -> mockPaymentService.createPayment(request));
    }

    @Test
    void invalidPasswordDoesNotCreatePayment() {
        MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();
        int paymentsBefore = mockPaymentService.getPaymentsByUser(1001L).size();
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), "USD", "wrong-password");

        assertThrows(InvalidPaymentPasswordException.class, () -> mockPaymentService.createPayment(request));
        assertEquals(paymentsBefore, mockPaymentService.getPaymentsByUser(1001L).size());
    }
}

