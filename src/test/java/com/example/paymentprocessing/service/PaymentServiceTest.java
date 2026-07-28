package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import com.example.paymentprocessing.exception.InvalidPaymentPasswordException;
import com.example.paymentprocessing.exception.PaymentNotFoundException;
import com.example.paymentprocessing.exception.UserNotFoundException;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.PaymentStatusHistoryRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 * Tests payment business logic including creation, retrieval, history lookup, and status updates.
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User sourceUser;
    private User destinationUser;
    private Payment payment;
    private PaymentStatusHistory history;

    @BeforeEach
    void setUp() {
        // Setup test data
        sourceUser = new User();
        sourceUser.setId(1L);
        sourceUser.setAccountNumber("ACC-10001");
        sourceUser.setBalance(new BigDecimal("5000.00"));
        sourceUser.setStatus(UserStatus.ACTIVE);
        sourceUser.setPaymentPasswordHash("hashedPassword");
        sourceUser.setCreatedAt(LocalDateTime.now());
        sourceUser.setUpdatedAt(LocalDateTime.now());

        destinationUser = new User();
        destinationUser.setId(2L);
        destinationUser.setAccountNumber("ACC-10002");
        destinationUser.setBalance(new BigDecimal("3200.00"));
        destinationUser.setStatus(UserStatus.ACTIVE);
        destinationUser.setPaymentPasswordHash("hashedPassword");
        destinationUser.setCreatedAt(LocalDateTime.now());
        destinationUser.setUpdatedAt(LocalDateTime.now());

        payment = new Payment();
        payment.setId(1L);
        payment.setSourceAccount(sourceUser);
        payment.setDestinationAccountId(2L);
        payment.setAmount(new BigDecimal("1500.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        history = new PaymentStatusHistory();
        history.setId(1L);
        history.setPayment(payment);
        history.setPreviousStatus(null);
        history.setNewStatus(PaymentStatus.CREATED);
        history.setChangedAt(LocalDateTime.now());
        history.setNotes("Payment created successfully");
    }

    // ============== Tests for createPayment ==============

    @Test
    void testCreatePayment_Success() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("1500.00"));
        request.setCurrency("USD");
        request.setPaymentPassword("password123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        when(passwordEncoder.matches("password123", sourceUser.getPaymentPasswordHash())).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentStatusHistoryRepository.save(any(PaymentStatusHistory.class))).thenReturn(history);

        // Act
        PaymentResponse response = paymentService.createPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());
        assertEquals(1L, response.getSourceAccountId());
        assertEquals(2L, response.getDestinationAccountId());
        assertEquals(new BigDecimal("1500.00"), response.getAmount());
        assertEquals(PaymentStatus.CREATED, response.getStatus());
        assertEquals("USD", response.getCurrency());

        verify(userRepository, times(2)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches("password123", sourceUser.getPaymentPasswordHash());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(1)).save(any(PaymentStatusHistory.class));
    }

    @Test
    void testCreatePayment_SourceUserNotFound() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSourceAccountId(999L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("1500.00"));
        request.setCurrency("USD");
        request.setPaymentPassword("password123");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> paymentService.createPayment(request));
    }

    @Test
    void testCreatePayment_DestinationUserNotFound() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(999L);
        request.setAmount(new BigDecimal("1500.00"));
        request.setCurrency("USD");
        request.setPaymentPassword("password123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> paymentService.createPayment(request));
    }

    @Test
    void testCreatePayment_InvalidPassword() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("1500.00"));
        request.setCurrency("USD");
        request.setPaymentPassword("wrongPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        when(passwordEncoder.matches("wrongPassword", sourceUser.getPaymentPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPaymentPasswordException.class, () -> paymentService.createPayment(request));
    }

    // ============== Tests for getPaymentById ==============

    @Test
    void testGetPaymentById_Success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(userRepository.findByAccountNumber("ACC-10002")).thenReturn(Optional.of(destinationUser));

        // Act
        PaymentResponse response = paymentService.getPaymentById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());
        assertEquals(1L, response.getSourceAccountId());
        assertEquals(2L, response.getDestinationAccountId());

        verify(paymentRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByAccountNumber("ACC-10002");
    }

    @Test
    void testGetPaymentById_PaymentNotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(999L));
    }

    // ============== Tests for getPaymentsByUser ==============

    @Test
    void testGetPaymentsByUser_Success() {
        // Arrange
        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setSourceAccount(sourceUser);
        payment2.setDestinationAccountId(3L);
        payment2.setAmount(new BigDecimal("500.00"));
        payment2.setCurrency("USD");
        payment2.setStatus(PaymentStatus.VALIDATED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(paymentRepository.findBySourceAccount_IdOrDestinationAccountId(1L, "ACC-10001"))
                .thenReturn(Arrays.asList(payment, payment2));
        when(userRepository.findByAccountNumber("ACC-10002")).thenReturn(Optional.of(destinationUser));
        when(userRepository.findByAccountNumber(any())).thenReturn(Optional.of(destinationUser));

        // Act
        List<PaymentResponse> responses = paymentService.getPaymentsByUser(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getPaymentId());
        assertEquals(2L, responses.get(1).getPaymentId());

        verify(userRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).findBySourceAccount_IdOrDestinationAccountId(1L, "ACC-10001");
    }

    @Test
    void testGetPaymentsByUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> paymentService.getPaymentsByUser(999L));
    }

    // ============== Tests for getPaymentHistory ==============

    @Test
    void testGetPaymentHistory_Success() {
        // Arrange
        PaymentStatusHistory history2 = new PaymentStatusHistory();
        history2.setId(2L);
        history2.setPayment(payment);
        history2.setPreviousStatus(PaymentStatus.CREATED);
        history2.setNewStatus(PaymentStatus.VALIDATED);
        history2.setChangedAt(LocalDateTime.now().plusMinutes(5));
        history2.setNotes("Payment validated");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(1L))
                .thenReturn(Arrays.asList(history, history2));

        // Act
        PaymentHistoryResponse response = paymentService.getPaymentHistory(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());
        assertEquals(2, response.getHistory().size());
        assertEquals(PaymentStatus.CREATED, response.getHistory().get(0).getNewStatus());
        assertEquals(PaymentStatus.VALIDATED, response.getHistory().get(1).getNewStatus());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentStatusHistoryRepository, times(1)).findByPayment_IdOrderByChangedAtAsc(1L);
    }

    @Test
    void testGetPaymentHistory_PaymentNotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentHistory(999L));
    }

    // ============== Tests for updatePaymentStatus ==============

    @Test
    void testUpdatePaymentStatus_ValidTransition_CreatedToValidated() {
        // Arrange
        Payment currentPayment = new Payment();
        currentPayment.setId(1L);
        currentPayment.setSourceAccount(sourceUser);
        currentPayment.setDestinationAccountId(2L);
        currentPayment.setAmount(new BigDecimal("1500.00"));
        currentPayment.setCurrency("USD");
        currentPayment.setStatus(PaymentStatus.CREATED);
        currentPayment.setCreatedAt(LocalDateTime.now());
        currentPayment.setUpdatedAt(LocalDateTime.now());

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.VALIDATED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(currentPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(currentPayment);
        when(paymentStatusHistoryRepository.save(any(PaymentStatusHistory.class))).thenReturn(history);
        when(userRepository.findByAccountNumber("ACC-10002")).thenReturn(Optional.of(destinationUser));

        // Act
        PaymentResponse response = paymentService.updatePaymentStatus(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, times(1)).save(any(PaymentStatusHistory.class));
    }

    @Test
    void testUpdatePaymentStatus_InvalidTransition_CompletedToFailed() {
        // Arrange
        Payment completedPayment = new Payment();
        completedPayment.setId(1L);
        completedPayment.setSourceAccount(sourceUser);
        completedPayment.setDestinationAccountId(2L);
        completedPayment.setAmount(new BigDecimal("1500.00"));
        completedPayment.setCurrency("USD");
        completedPayment.setStatus(PaymentStatus.COMPLETED);

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paymentService.updatePaymentStatus(1L, request));
    }

    @Test
    void testUpdatePaymentStatus_PaymentNotFound() {
        // Arrange
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.VALIDATED);

        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentNotFoundException.class, () -> paymentService.updatePaymentStatus(999L, request));
    }

    @Test
    void testUpdatePaymentStatus_ValidTransition_ValidatedToSent() {
        // Arrange
        Payment validatedPayment = new Payment();
        validatedPayment.setId(1L);
        validatedPayment.setSourceAccount(sourceUser);
        validatedPayment.setDestinationAccountId(2L);
        validatedPayment.setAmount(new BigDecimal("1500.00"));
        validatedPayment.setCurrency("USD");
        validatedPayment.setStatus(PaymentStatus.VALIDATED);
        validatedPayment.setCreatedAt(LocalDateTime.now());
        validatedPayment.setUpdatedAt(LocalDateTime.now());

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.SENT);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(validatedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(validatedPayment);
        when(paymentStatusHistoryRepository.save(any(PaymentStatusHistory.class))).thenReturn(history);
        when(userRepository.findByAccountNumber("ACC-10002")).thenReturn(Optional.of(destinationUser));

        // Act
        PaymentResponse response = paymentService.updatePaymentStatus(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_ValidTransition_SentToCompleted() {
        // Arrange
        Payment sentPayment = new Payment();
        sentPayment.setId(1L);
        sentPayment.setSourceAccount(sourceUser);
        sentPayment.setDestinationAccountId(2L);
        sentPayment.setAmount(new BigDecimal("1500.00"));
        sentPayment.setCurrency("USD");
        sentPayment.setStatus(PaymentStatus.SENT);
        sentPayment.setCreatedAt(LocalDateTime.now());
        sentPayment.setUpdatedAt(LocalDateTime.now());

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(sentPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(sentPayment);
        when(paymentStatusHistoryRepository.save(any(PaymentStatusHistory.class))).thenReturn(history);
        when(userRepository.findByAccountNumber("ACC-10002")).thenReturn(Optional.of(destinationUser));

        // Act
        PaymentResponse response = paymentService.updatePaymentStatus(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}

