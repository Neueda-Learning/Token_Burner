package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.exception.InvalidPaymentPasswordException;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.PaymentStatusHistoryRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
    void activeAccountsWithinLimitRemainCreatedAfterCreation() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("5000.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        stubPaymentSaveWithTimestamps();

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("USD", response.currency());
        assertEquals(101L, response.paymentId());
        assertEquals(PaymentStatus.CREATED, response.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(1)).save(historyCaptor.capture());

        PaymentStatusHistory createdHistory = historyCaptor.getValue();
        assertNull(createdHistory.getPreviousStatus());
        assertEquals(PaymentStatus.CREATED, createdHistory.getNewStatus());
        assertEquals("Payment created successfully", createdHistory.getNotes());
    }

    @Test
    void inactiveSourceAccountCreatesFailedPaymentWithHistory() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        User sourceUser = buildUser(1L, new BigDecimal("5000.00"), UserStatus.INACTIVE);
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        stubPaymentSaveWithTimestamps();

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, response.status());
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(2)).save(historyCaptor.capture());

        List<PaymentStatusHistory> histories = historyCaptor.getAllValues();
        assertEquals(PaymentStatus.CREATED, histories.get(0).getNewStatus());
        assertEquals(PaymentStatus.CREATED, histories.get(1).getPreviousStatus());
        assertEquals(PaymentStatus.FAILED, histories.get(1).getNewStatus());
        assertEquals("Payment failed: source or destination account is inactive", histories.get(1).getNotes());
    }

    @Test
    void inactiveDestinationAccountCreatesFailedPaymentWithHistory() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("5000.00"));
        User destinationUser = buildUser(2L, new BigDecimal("800.00"), UserStatus.INACTIVE);
        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        stubPaymentSaveWithTimestamps();

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, response.status());
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(2)).save(historyCaptor.capture());

        PaymentStatusHistory failedHistory = historyCaptor.getAllValues().get(1);
        assertEquals(PaymentStatus.CREATED, failedHistory.getPreviousStatus());
        assertEquals(PaymentStatus.FAILED, failedHistory.getNewStatus());
        assertEquals("Payment failed: source or destination account is inactive", failedHistory.getNotes());
    }

    @Test
    void amountExactlySinglePaymentLimitIsAllowed() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("1000000.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("1000000.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        stubPaymentSaveWithTimestamps();

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.CREATED, response.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(1)).save(any(PaymentStatusHistory.class));
    }

    @Test
    void amountAboveSinglePaymentLimitCreatesFailedPaymentWithHistory() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("1000000.01"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("2000000.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        stubPaymentSaveWithTimestamps();

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, response.status());
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(2)).save(historyCaptor.capture());

        PaymentStatusHistory failedHistory = historyCaptor.getAllValues().get(1);
        assertEquals(PaymentStatus.CREATED, failedHistory.getPreviousStatus());
        assertEquals(PaymentStatus.FAILED, failedHistory.getNewStatus());
        assertEquals("Payment failed: transaction amount exceeds the single-payment limit", failedHistory.getNotes());
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

    @Test
    void invalidPasswordDoesNotCreatePaymentOrHistory() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "wrong-password");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidPaymentPasswordException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    private void stubPaymentSaveWithTimestamps() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(101L);
            }
            if (payment.getStatus() == null) {
                payment.setStatus(PaymentStatus.CREATED);
            }
            if (payment.getCreatedAt() == null) {
                payment.setCreatedAt(LocalDateTime.of(2026, 7, 28, 10, 0));
            }
            payment.setUpdatedAt(LocalDateTime.of(2026, 7, 28, 10, payment.getStatus() == PaymentStatus.FAILED ? 1 : 0));
            return payment;
        });
    }

    private User buildActiveUser(Long id, BigDecimal balance) {
        return buildUser(id, balance, UserStatus.ACTIVE);
    }

    private User buildUser(Long id, BigDecimal balance, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setAccountNumber("ACC-" + id);
        user.setBalance(balance);
        user.setStatus(status);
        user.setPaymentPasswordHash("123456");
        return user;
    }
}

