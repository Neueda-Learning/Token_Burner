package com.example.paymentprocessing.repository;
//todo Kylian


import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class PaymentStatusHistoryRepositoryTest {

    @Autowired
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
        // 这个用例验证按 paymentId 查询历史时，会按照 changedAt 升序返回，保证服务层拿到的是时间顺序数据。
    void findByPaymentIdOrderByChangedAtAscReturnsSortedHistory() {
        User sourceUser = persistUser("ACC-HISTORY-1001");
        Payment payment = persistPayment(sourceUser, 2002L, new BigDecimal("120.00"), "USD", PaymentStatus.CREATED);

        PaymentStatusHistory latest = persistHistory(
                payment,
                PaymentStatus.VALIDATED,
                PaymentStatus.SENT,
                LocalDateTime.of(2026, 7, 30, 11, 20, 0),
                "sent"
        );
        PaymentStatusHistory earliest = persistHistory(
                payment,
                PaymentStatus.CREATED,
                PaymentStatus.VALIDATED,
                LocalDateTime.of(2026, 7, 30, 11, 10, 0),
                "validated"
        );
        PaymentStatusHistory middle = persistHistory(
                payment,
                PaymentStatus.SENT,
                PaymentStatus.COMPLETED,
                LocalDateTime.of(2026, 7, 30, 11, 15, 0),
                "completed"
        );

        List<PaymentStatusHistory> result = paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(payment.getId());

        assertEquals(3, result.size());
        assertEquals(earliest.getId(), result.get(0).getId());
        assertEquals(middle.getId(), result.get(1).getId());
        assertEquals(latest.getId(), result.get(2).getId());
    }

    @Test
        // 这个用例验证当支付单没有任何历史记录时，仓储查询会返回空列表而不是抛异常。
    void findByPaymentIdOrderByChangedAtAscReturnsEmptyWhenNoHistory() {
        User sourceUser = persistUser("ACC-HISTORY-1002");
        Payment payment = persistPayment(sourceUser, 3003L, new BigDecimal("88.00"), "EUR", PaymentStatus.CREATED);

        List<PaymentStatusHistory> result = paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(payment.getId());

        assertTrue(result.isEmpty());
    }

    @Test
        // 这个用例验证保存历史记录时关键字段会被正确持久化，并且 changedAt 为空时会由实体自动填充。
    void savePersistsFieldsAndAutoFillsChangedAt() {
        User sourceUser = persistUser("ACC-HISTORY-1003");
        Payment payment = persistPayment(sourceUser, 4004L, new BigDecimal("66.00"), "GBP", PaymentStatus.CREATED);

        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPayment(payment);
        history.setPreviousStatus(PaymentStatus.CREATED);
        history.setNewStatus(PaymentStatus.VALIDATED);
        history.setNotes("auto changedAt");

        PaymentStatusHistory saved = paymentStatusHistoryRepository.saveAndFlush(history);

        assertNotNull(saved.getId());
        assertEquals(PaymentStatus.CREATED, saved.getPreviousStatus());
        assertEquals(PaymentStatus.VALIDATED, saved.getNewStatus());
        assertEquals("auto changedAt", saved.getNotes());
        assertNotNull(saved.getChangedAt());
    }

    // 这个辅助函数用于持久化测试用户，统一准备支付与历史记录依赖的账户数据。
    private User persistUser(String accountNumber) {
        User user = new User();
        user.setAccountNumber(accountNumber);
        user.setBalance(new BigDecimal("1000.00"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPaymentPasswordHash("pwd-123");
        return entityManager.persistAndFlush(user);
    }

    // 这个辅助函数用于持久化支付主记录，避免每个测试重复组装支付实体字段。
    private Payment persistPayment(
            User sourceAccount,
            Long destinationAccountId,
            BigDecimal amount,
            String currency,
            PaymentStatus status
    ) {
        Payment payment = new Payment();
        payment.setSourceAccount(sourceAccount);
        payment.setDestinationAccountId(destinationAccountId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(status);
        return entityManager.persistAndFlush(payment);
    }

    // 这个辅助函数用于持久化状态历史记录，并允许显式设置 changedAt 以验证排序语义。
    private PaymentStatusHistory persistHistory(
            Payment payment,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            LocalDateTime changedAt,
            String notes
    ) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPayment(payment);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(changedAt);
        history.setNotes(notes);
        return entityManager.persistAndFlush(history);
    }
}
