package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MockPaymentServiceImplTest {

    private final MockPaymentServiceImpl mockPaymentService = new MockPaymentServiceImpl();

    @Test
    void validCurrencyCreatesPaymentSuccessfully() {
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), "EUR", "888888");

        PaymentResponse response = mockPaymentService.createPayment(request);

        assertEquals("EUR", response.currency());
    }

    @Test
    void unsupportedCurrencyThrowsInvalidCurrencyException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), "ABC", "888888");

        assertThrows(InvalidCurrencyException.class, () -> mockPaymentService.createPayment(request));
    }

    @Test
    void nullCurrencyThrowsInvalidCurrencyException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1001L, 1002L, new BigDecimal("250.00"), null, "888888");

        assertThrows(InvalidCurrencyException.class, () -> mockPaymentService.createPayment(request));
    }
}

