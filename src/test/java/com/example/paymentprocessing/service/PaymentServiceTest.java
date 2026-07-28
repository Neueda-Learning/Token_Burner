package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.PaymentStatusHistoryRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void validCurrencyCreatesPaymentSuccessfully() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(101L);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.of(2026, 7, 28, 10, 0));
            payment.setUpdatedAt(LocalDateTime.of(2026, 7, 28, 10, 0));
            return payment;
        });

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("USD", response.currency());
        assertEquals(101L, response.paymentId());
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentStatusHistoryRepository).save(any());
    }

    @Test
    void unsupportedCurrencyThrowsInvalidCurrencyException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "ABC", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidCurrencyException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
    void nullCurrencyThrowsInvalidCurrencyException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), null, "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidCurrencyException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    private User buildActiveUser(Long id, BigDecimal balance) {
        User user = new User();
        user.setId(id);
        user.setAccountNumber("ACC-" + id);
        user.setBalance(balance);
        user.setStatus(UserStatus.ACTIVE);
        user.setPaymentPasswordHash("123456");
        return user;
    }
}

