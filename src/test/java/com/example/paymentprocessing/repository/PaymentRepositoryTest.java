package com.example.paymentprocessing.repository;



//todo Kylian



import com.example.paymentprocessing.entity.Payment;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
        // 测试按用户查询时会返回该用户作为付款方或收款方的所有支付记录。
    void findBySourceAccountIdOrDestinationAccountIdReturnsRelatedPayments() {
        User userA = persistUser("ACC-1001");
        User userB = persistUser("ACC-1002");
        User userC = persistUser("ACC-1003");

        Payment first = persistPayment(userA, userB.getId(), new BigDecimal("100.00"), PaymentStatus.CREATED);
        Payment second = persistPayment(userC, userA.getId(), new BigDecimal("200.00"), PaymentStatus.SENT);
        persistPayment(userB, userC.getId(), new BigDecimal("300.00"), PaymentStatus.COMPLETED);

        List<Payment> related = paymentRepository.findBySourceAccount_IdOrDestinationAccountId(userA.getId(), userA.getId());
        Set<Long> relatedIds = related.stream().map(Payment::getId).collect(Collectors.toSet());

        assertEquals(2, related.size());
        assertEquals(Set.of(first.getId(), second.getId()), relatedIds);
    }

    @Test
        // 测试按状态计数时会返回该状态对应的准确记录数量。
    void countByStatusReturnsExpectedCount() {
        User source = persistUser("ACC-2001");

        persistPayment(source, 9001L, new BigDecimal("10.00"), PaymentStatus.CREATED);
        persistPayment(source, 9002L, new BigDecimal("20.00"), PaymentStatus.CREATED);
        persistPayment(source, 9003L, new BigDecimal("30.00"), PaymentStatus.SENT);

        long createdCount = paymentRepository.countByStatus(PaymentStatus.CREATED);
        long completedCount = paymentRepository.countByStatus(PaymentStatus.COMPLETED);

        assertEquals(2L, createdCount);
        assertEquals(0L, completedCount);
    }

    @Test
        // 测试汇总金额查询时会返回所有支付金额的总和。
    void sumAllAmountsReturnsExpectedTotal() {
        User source = persistUser("ACC-3001");

        persistPayment(source, 9101L, new BigDecimal("10.50"), PaymentStatus.CREATED);
        persistPayment(source, 9102L, new BigDecimal("20.25"), PaymentStatus.SENT);
        persistPayment(source, 9103L, new BigDecimal("30.25"), PaymentStatus.COMPLETED);

        BigDecimal total = paymentRepository.sumAllAmounts();

        assertNotNull(total);
        assertEquals(new BigDecimal("61.00"), total);
    }

    @Test
        // 测试分组统计时会按状态返回每种状态对应的支付数量。
    void countPaymentsGroupedByStatusReturnsGroupedCounts() {
        User source = persistUser("ACC-4001");

        persistPayment(source, 9201L, new BigDecimal("15.00"), PaymentStatus.CREATED);
        persistPayment(source, 9202L, new BigDecimal("16.00"), PaymentStatus.CREATED);
        persistPayment(source, 9203L, new BigDecimal("17.00"), PaymentStatus.SENT);
        persistPayment(source, 9204L, new BigDecimal("18.00"), PaymentStatus.FAILED);

        List<PaymentRepository.PaymentStatusCountProjection> grouped = paymentRepository.countPaymentsGroupedByStatus();
        Map<PaymentStatus, Long> countByStatus = grouped.stream()
                .collect(Collectors.toMap(PaymentRepository.PaymentStatusCountProjection::getStatus,
                        PaymentRepository.PaymentStatusCountProjection::getTotal));

        assertEquals(3, grouped.size());
        assertEquals(2L, countByStatus.get(PaymentStatus.CREATED));
        assertEquals(1L, countByStatus.get(PaymentStatus.SENT));
        assertEquals(1L, countByStatus.get(PaymentStatus.FAILED));
    }

    // 构造并持久化一个可用于支付关联的激活用户。
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

    // 构造并持久化一条支付记录，便于仓储查询断言复用。
    private Payment persistPayment(User source, Long destinationAccountId, BigDecimal amount, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setSourceAccount(source);
        payment.setDestinationAccountId(destinationAccountId);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setStatus(status);
        payment.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        payment.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return entityManager.persistAndFlush(payment);
    }
}

