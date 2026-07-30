package com.example.paymentprocessing.repository;


//todo Kylian


import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class PaymentStatusHistoryRepositoryTest {

    @Autowired
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
        // 测试按支付单号查询历史时会按照 changedAt 升序返回记录。
    void findByPaymentIdOrderByChangedAtAscReturnsSortedHistory() {
        User source = persistUser("ACC-H-1001");
        Payment payment = persistPayment(source, 8101L, new BigDecimal("88.00"));

        PaymentStatusHistory second = persistHistory(
                payment,
                PaymentStatus.VALIDATED,
                PaymentStatus.SENT,
                LocalDateTime.of(2026, 1, 1, 10, 30),
                "sent"
        );
        PaymentStatusHistory first = persistHistory(
                payment,
                PaymentStatus.CREATED,
                PaymentStatus.VALIDATED,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "validated"
        );

        List<PaymentStatusHistory> histories = paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(payment.getId());

        assertEquals(2, histories.size());
        assertEquals(first.getId(), histories.get(0).getId());
        assertEquals(second.getId(), histories.get(1).getId());
    }

    @Test
        // 测试按支付单号查询历史时只会返回该支付单对应的状态变更记录。
    void findByPaymentIdOrderByChangedAtAscReturnsOnlyTargetPaymentHistory() {
        User source = persistUser("ACC-H-2001");
        Payment targetPayment = persistPayment(source, 8201L, new BigDecimal("66.00"));
        Payment otherPayment = persistPayment(source, 8202L, new BigDecimal("77.00"));

        persistHistory(targetPayment, PaymentStatus.CREATED, PaymentStatus.VALIDATED, LocalDateTime.of(2026, 1, 2, 9, 0), "target-1");
        persistHistory(targetPayment, PaymentStatus.VALIDATED, PaymentStatus.SENT, LocalDateTime.of(2026, 1, 2, 10, 0), "target-2");
        persistHistory(otherPayment, PaymentStatus.CREATED, PaymentStatus.FAILED, LocalDateTime.of(2026, 1, 2, 11, 0), "other");

        List<PaymentStatusHistory> histories = paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(targetPayment.getId());

        assertEquals(2, histories.size());
        assertTrue(histories.stream().allMatch(history -> history.getPayment().getId().equals(targetPayment.getId())));
    }

    @Test
        // 测试按支付单号查询历史时在无历史记录场景下会返回空列表。
    void findByPaymentIdOrderByChangedAtAscReturnsEmptyWhenNoHistory() {
        User source = persistUser("ACC-H-3001");
        Payment payment = persistPayment(source, 8301L, new BigDecimal("99.00"));

        List<PaymentStatusHistory> histories = paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(payment.getId());

        assertTrue(histories.isEmpty());
    }

    // 构造并持久化一个激活用户，供支付与历史实体关联使用。
    private User persistUser(String accountNumber) {
        User user = new User();
        user.setAccountNumber(accountNumber);
        user.setBalance(new BigDecimal("1000.00"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPaymentPasswordHash("123456");
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return entityManager.persistAndFlush(user);
    }

    // 构造并持久化一条支付记录，供历史记录测试场景复用。
    private Payment persistPayment(User source, Long destinationAccountId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setSourceAccount(source);
        payment.setDestinationAccountId(destinationAccountId);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        payment.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return entityManager.persistAndFlush(payment);
    }

    // 构造并持久化一条状态历史记录，便于按时间和支付单号进行断言。
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

