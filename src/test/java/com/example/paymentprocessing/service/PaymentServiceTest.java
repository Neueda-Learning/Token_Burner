package com.example.paymentprocessing.service;
//todo Kylian



import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import com.example.paymentprocessing.exception.InsufficientBalanceException;
import com.example.paymentprocessing.exception.InvalidAccountStatusException;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.exception.InvalidPaymentAmountException;
import com.example.paymentprocessing.exception.InvalidPaymentPasswordException;
import com.example.paymentprocessing.exception.InvalidPaymentStatusException;
import com.example.paymentprocessing.exception.PaymentNotFoundException;
import com.example.paymentprocessing.exception.UserNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 7, 28, 10, 0);

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
        // 测试当请求参数合法时，createPayment 会创建支付并写入状态历史。
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
            payment.setCreatedAt(FIXED_TIME);
            payment.setUpdatedAt(FIXED_TIME);
            return payment;
        });

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("USD", response.currency());
        assertEquals(101L, response.paymentId());
        assertEquals(PaymentStatus.CREATED, response.status());
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentStatusHistoryRepository).save(any());
    }

    @Test
        // 测试当币种带空格和小写时，createPayment 会做标准化并以大写币种保存。
    void currencyIsNormalizedBeforeSaving() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), " eur ", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(102L);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(FIXED_TIME);
            payment.setUpdatedAt(FIXED_TIME);
            return payment;
        });

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("EUR", response.currency());
    }

    @Test
        // 测试当币种不在支持列表内时，createPayment 会抛出 InvalidCurrencyException。
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
        // 测试当币种为 null 时，createPayment 会抛出 InvalidCurrencyException。
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
        // 测试当源用户不存在时，createPayment 会抛出 UserNotFoundException。
    void sourceUserNotFoundThrowsUserNotFoundException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试当目标用户不存在时，createPayment 会抛出 UserNotFoundException。
    void destinationUserNotFoundThrowsUserNotFoundException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试当任一账户状态非 ACTIVE 时，createPayment 会抛出 InvalidAccountStatusException。
    void inactiveAccountThrowsInvalidAccountStatusException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        destinationUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidAccountStatusException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试当金额为 null 时，createPayment 会抛出 InvalidPaymentAmountException。
    void nullAmountThrowsInvalidPaymentAmountException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, null, "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidPaymentAmountException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试当金额小于等于 0 时，createPayment 会抛出 InvalidPaymentAmountException。
    void nonPositiveAmountThrowsInvalidPaymentAmountException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, BigDecimal.ZERO, "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidPaymentAmountException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试当支付密码不匹配时，createPayment 会抛出 InvalidPaymentPasswordException。
    void invalidPaymentPasswordThrowsInvalidPaymentPasswordException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("150.00"), "USD", "wrong-password");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InvalidPaymentPasswordException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试当余额不足时，createPayment 会抛出 InsufficientBalanceException。
    void insufficientBalanceThrowsInsufficientBalanceException() {
        CreatePaymentRequest request = new CreatePaymentRequest(1L, 2L, new BigDecimal("900.00"), "USD", "123456");
        User sourceUser = buildActiveUser(1L, new BigDecimal("500.00"));
        User destinationUser = buildActiveUser(2L, new BigDecimal("800.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sourceUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinationUser));

        assertThrows(InsufficientBalanceException.class, () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusHistoryRepository, never()).save(any());
    }

    @Test
        // 测试根据支付单号查询时，getPaymentById 会返回映射后的支付详情。
    void getPaymentByIdReturnsMappedResponse() {
        Payment payment = buildPayment(101L, 1L, 2L, new BigDecimal("188.00"), "USD", PaymentStatus.SENT);
        when(paymentRepository.findById(101L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(101L);

        assertEquals(101L, response.paymentId());
        assertEquals(1L, response.sourceAccountId());
        assertEquals(2L, response.destinationAccountId());
        assertEquals(new BigDecimal("188.00"), response.amount());
        assertEquals("USD", response.currency());
        assertEquals(PaymentStatus.SENT, response.status());
        assertEquals(FIXED_TIME, response.createdAt());
        assertEquals(FIXED_TIME, response.updatedAt());
    }

    @Test
        // 测试查询不存在的支付单号时，getPaymentById 会抛出 PaymentNotFoundException。
    void getPaymentByIdNotFoundThrowsPaymentNotFoundException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(999L));
    }

    @Test
        // 测试查询支付历史时，getPaymentHistory 会按仓储结果映射并返回历史列表。
    void getPaymentHistoryReturnsMappedResponses() {
        Payment payment = buildPayment(200L, 1L, 2L, new BigDecimal("300.00"), "GBP", PaymentStatus.SENT);
        PaymentStatusHistory first = buildHistory(1L, payment, PaymentStatus.CREATED, PaymentStatus.VALIDATED, "validated");
        PaymentStatusHistory second = buildHistory(2L, payment, PaymentStatus.VALIDATED, PaymentStatus.SENT, "sent");
        when(paymentRepository.existsById(200L)).thenReturn(true);
        when(paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(200L)).thenReturn(List.of(first, second));

        List<PaymentHistoryResponse> responses = paymentService.getPaymentHistory(200L);

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).historyId());
        assertEquals(PaymentStatus.CREATED, responses.get(0).previousStatus());
        assertEquals(PaymentStatus.VALIDATED, responses.get(0).newStatus());
        assertEquals(2L, responses.get(1).historyId());
        assertEquals(PaymentStatus.VALIDATED, responses.get(1).previousStatus());
        assertEquals(PaymentStatus.SENT, responses.get(1).newStatus());
    }

    @Test
        // 测试查询不存在支付的历史时，getPaymentHistory 会抛出 PaymentNotFoundException。
    void getPaymentHistoryNotFoundThrowsPaymentNotFoundException() {
        when(paymentRepository.existsById(300L)).thenReturn(false);

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentHistory(300L));
    }

    @Test
        // 测试查询用户相关支付时，getPaymentsByUser 会返回该用户作为源或目标的支付记录。
    void getPaymentsByUserReturnsMappedResponses() {
        Payment first = buildPayment(301L, 10L, 20L, new BigDecimal("50.00"), "USD", PaymentStatus.CREATED);
        Payment second = buildPayment(302L, 99L, 10L, new BigDecimal("70.00"), "EUR", PaymentStatus.COMPLETED);
        when(userRepository.existsById(10L)).thenReturn(true);
        when(paymentRepository.findBySourceAccount_IdOrDestinationAccountId(10L, 10L)).thenReturn(List.of(first, second));

        List<PaymentResponse> responses = paymentService.getPaymentsByUser(10L);

        assertEquals(2, responses.size());
        assertEquals(301L, responses.get(0).paymentId());
        assertEquals(302L, responses.get(1).paymentId());
    }

    @Test
        // 测试查询存在但无支付记录的用户时，getPaymentsByUser 会返回空列表。
    void getPaymentsByUserReturnsEmptyListWhenNoPayments() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(paymentRepository.findBySourceAccount_IdOrDestinationAccountId(10L, 10L)).thenReturn(List.of());

        List<PaymentResponse> responses = paymentService.getPaymentsByUser(10L);

        assertTrue(responses.isEmpty());
    }

    @Test
        // 测试查询不存在用户的支付记录时，getPaymentsByUser 会抛出 UserNotFoundException。
    void getPaymentsByUserUserNotFoundThrowsUserNotFoundException() {
        when(userRepository.existsById(10L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> paymentService.getPaymentsByUser(10L));
    }

    @Test
        // 测试合法状态流转时，updatePaymentStatus 会更新状态并写入一条历史记录。
    void updatePaymentStatusWithValidTransitionUpdatesPaymentAndSavesHistory() {
        Payment payment = buildPayment(500L, 1L, 2L, new BigDecimal("60.00"), "USD", PaymentStatus.CREATED);
        when(paymentRepository.findById(500L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.updatePaymentStatus(500L, new UpdatePaymentStatusRequest(PaymentStatus.VALIDATED));

        assertEquals(PaymentStatus.VALIDATED, response.status());

        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);
        verify(paymentStatusHistoryRepository).save(historyCaptor.capture());
        PaymentStatusHistory savedHistory = historyCaptor.getValue();
        assertEquals(PaymentStatus.CREATED, savedHistory.getPreviousStatus());
        assertEquals(PaymentStatus.VALIDATED, savedHistory.getNewStatus());
        assertEquals("Payment request validated", savedHistory.getNotes());
        assertSame(payment, savedHistory.getPayment());
    }

    @Test
        // 测试非法状态流转时，updatePaymentStatus 会抛出 InvalidPaymentStatusException。
    void updatePaymentStatusWithInvalidTransitionThrowsInvalidPaymentStatusException() {
        Payment payment = buildPayment(500L, 1L, 2L, new BigDecimal("60.00"), "USD", PaymentStatus.CREATED);
        when(paymentRepository.findById(500L)).thenReturn(Optional.of(payment));

        assertThrows(
                InvalidPaymentStatusException.class,
                () -> paymentService.updatePaymentStatus(500L, new UpdatePaymentStatusRequest(PaymentStatus.COMPLETED))
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentStatusHistoryRepository, never()).save(any(PaymentStatusHistory.class));
    }

    @Test
        // 测试更新不存在支付单状态时，updatePaymentStatus 会抛出 PaymentNotFoundException。
    void updatePaymentStatusPaymentNotFoundThrowsPaymentNotFoundException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.updatePaymentStatus(999L, new UpdatePaymentStatusRequest(PaymentStatus.VALIDATED))
        );
    }

    @Test
        // 测试状态流转规则中允许的路径会返回 true。
    void isTransitionAllowedReturnsTrueForValidPaths() {
        assertTrue(paymentService.isTransitionAllowed(PaymentStatus.CREATED, PaymentStatus.VALIDATED));
        assertTrue(paymentService.isTransitionAllowed(PaymentStatus.CREATED, PaymentStatus.FAILED));
        assertTrue(paymentService.isTransitionAllowed(PaymentStatus.VALIDATED, PaymentStatus.SENT));
        assertTrue(paymentService.isTransitionAllowed(PaymentStatus.SENT, PaymentStatus.COMPLETED));
    }

    @Test
        // 测试状态流转规则中不允许的路径会返回 false。
    void isTransitionAllowedReturnsFalseForInvalidPaths() {
        assertEquals(false, paymentService.isTransitionAllowed(PaymentStatus.CREATED, PaymentStatus.COMPLETED));
        assertEquals(false, paymentService.isTransitionAllowed(PaymentStatus.COMPLETED, PaymentStatus.FAILED));
        assertEquals(false, paymentService.isTransitionAllowed(PaymentStatus.CREATED, PaymentStatus.CREATED));
        assertEquals(false, paymentService.isTransitionAllowed(null, PaymentStatus.CREATED));
    }

    @Test
        // 测试构造非法流转提示时，buildInvalidTransitionMessage 会返回完整错误信息。
    void buildInvalidTransitionMessageBuildsExpectedText() {
        String message = paymentService.buildInvalidTransitionMessage(PaymentStatus.CREATED, PaymentStatus.SENT);
        assertEquals("Cannot change payment status from CREATED to SENT", message);
    }

    @Test
        // 测试构造状态备注时，buildTransitionNotes 会为每个状态返回固定文案。
    void buildTransitionNotesReturnsExpectedNotes() {
        assertEquals("Payment created successfully", paymentService.buildTransitionNotes(PaymentStatus.CREATED));
        assertEquals("Payment request validated", paymentService.buildTransitionNotes(PaymentStatus.VALIDATED));
        assertEquals("Payment sent for processing", paymentService.buildTransitionNotes(PaymentStatus.SENT));
        assertEquals("Payment completed successfully", paymentService.buildTransitionNotes(PaymentStatus.COMPLETED));
        assertEquals("Payment marked as failed", paymentService.buildTransitionNotes(PaymentStatus.FAILED));
    }

    @Test
        // 测试构造历史实体时，buildHistoryRecord 会正确填充支付、状态和备注字段。
    void buildHistoryRecordBuildsExpectedEntity() {
        Payment payment = buildPayment(701L, 1L, 2L, new BigDecimal("10.00"), "USD", PaymentStatus.CREATED);

        PaymentStatusHistory history = paymentService.buildHistoryRecord(
                payment,
                PaymentStatus.CREATED,
                PaymentStatus.VALIDATED,
                "validated"
        );

        assertNotNull(history);
        assertSame(payment, history.getPayment());
        assertEquals(PaymentStatus.CREATED, history.getPreviousStatus());
        assertEquals(PaymentStatus.VALIDATED, history.getNewStatus());
        assertEquals("validated", history.getNotes());
    }

    // 构造一个激活状态用户，供 createPayment 相关测试复用。
    private User buildActiveUser(Long id, BigDecimal balance) {
        User user = new User();
        user.setId(id);
        user.setAccountNumber("ACC-" + id);
        user.setBalance(balance);
        user.setStatus(UserStatus.ACTIVE);
        user.setPaymentPasswordHash("123456");
        return user;
    }

    // 构造一个完整支付实体，供查询和状态变更场景复用。
    private Payment buildPayment(
            Long paymentId,
            Long sourceUserId,
            Long destinationAccountId,
            BigDecimal amount,
            String currency,
            PaymentStatus status
    ) {
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setSourceAccount(buildActiveUser(sourceUserId, new BigDecimal("9999.99")));
        payment.setDestinationAccountId(destinationAccountId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(status);
        payment.setCreatedAt(FIXED_TIME);
        payment.setUpdatedAt(FIXED_TIME);
        return payment;
    }

    // 构造一条支付状态历史实体，供历史映射断言复用。
    private PaymentStatusHistory buildHistory(
            Long historyId,
            Payment payment,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            String notes
    ) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setId(historyId);
        history.setPayment(payment);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(FIXED_TIME);
        history.setNotes(notes);
        return history;
    }
}
