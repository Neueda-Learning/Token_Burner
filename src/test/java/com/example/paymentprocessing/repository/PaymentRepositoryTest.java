package com.example.paymentprocessing.repository;
//todo Kylian
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
        // 这个用例验证当用户既可能是付款方也可能是收款方时，仓储方法会把两类关联支付都正确查出来。
    void findBySourceAccountIdOrDestinationAccountIdReturnsRelatedPayments() {
        User alice = persistUser("ACC-1001");
        User bob = persistUser("ACC-1002");
        User carol = persistUser("ACC-1003");

        Payment aliceToBob = persistPayment(alice, bob.getId(), new BigDecimal("100.00"), "USD", PaymentStatus.CREATED);
        Payment bobToAlice = persistPayment(bob, alice.getId(), new BigDecimal("35.00"), "EUR", PaymentStatus.SENT);
        persistPayment(carol, bob.getId(), new BigDecimal("20.00"), "GBP", PaymentStatus.FAILED);

        List<Payment> result = paymentRepository.findBySourceAccount_IdOrDestinationAccountId(alice.getId(), alice.getId());

        assertEquals(2, result.size());
        List<Long> paymentIds = result.stream().map(Payment::getId).toList();
        assertTrue(paymentIds.contains(aliceToBob.getId()));
        assertTrue(paymentIds.contains(bobToAlice.getId()));
    }

    @Test
        // 这个用例验证按状态计数时，仓储可以只统计目标状态且不混入其他状态的记录。
    void countByStatusReturnsOnlyTargetStatusCount() {
        User alice = persistUser("ACC-2001");
        User bob = persistUser("ACC-2002");

        persistPayment(alice, bob.getId(), new BigDecimal("80.00"), "USD", PaymentStatus.CREATED);
        persistPayment(alice, bob.getId(), new BigDecimal("120.00"), "USD", PaymentStatus.CREATED);
        persistPayment(alice, bob.getId(), new BigDecimal("50.00"), "USD", PaymentStatus.COMPLETED);

        long createdCount = paymentRepository.countByStatus(PaymentStatus.CREATED);
        long completedCount = paymentRepository.countByStatus(PaymentStatus.COMPLETED);

        assertEquals(2L, createdCount);
        assertEquals(1L, completedCount);
    }

    @Test
        // 这个用例验证总金额聚合查询会把所有支付金额累计起来并返回正确的 BigDecimal 结果。
    void sumAllAmountsReturnsTotalAmount() {
        User alice = persistUser("ACC-3001");
        User bob = persistUser("ACC-3002");

        persistPayment(alice, bob.getId(), new BigDecimal("100.00"), "USD", PaymentStatus.CREATED);
        persistPayment(alice, bob.getId(), new BigDecimal("75.50"), "EUR", PaymentStatus.SENT);

        BigDecimal totalAmount = paymentRepository.sumAllAmounts();

        assertNotNull(totalAmount);
        assertEquals(0, totalAmount.compareTo(new BigDecimal("175.50")));
    }

    @Test
        // 这个用例验证当没有任何支付数据时，总金额聚合查询会返回 null 而不是错误值。
    void sumAllAmountsReturnsNullWhenNoPayments() {
        BigDecimal totalAmount = paymentRepository.sumAllAmounts();
        assertNull(totalAmount);
    }

    @Test
        // 这个用例验证按状态分组统计会为每个出现过的状态返回对应条数，确保报表类查询可直接使用。
    void countPaymentsGroupedByStatusReturnsGroupedCounts() {
        User alice = persistUser("ACC-4001");
        User bob = persistUser("ACC-4002");

        persistPayment(alice, bob.getId(), new BigDecimal("30.00"), "USD", PaymentStatus.CREATED);
        persistPayment(alice, bob.getId(), new BigDecimal("40.00"), "USD", PaymentStatus.CREATED);
        persistPayment(alice, bob.getId(), new BigDecimal("50.00"), "USD", PaymentStatus.FAILED);

        List<PaymentRepository.PaymentStatusCountProjection> grouped = paymentRepository.countPaymentsGroupedByStatus();
        Map<PaymentStatus, Long> countByStatus = grouped.stream()
                .collect(Collectors.toMap(
                        PaymentRepository.PaymentStatusCountProjection::getStatus,
                        PaymentRepository.PaymentStatusCountProjection::getTotal,
                        Long::sum
                ));

        assertEquals(2L, countByStatus.get(PaymentStatus.CREATED));
        assertEquals(1L, countByStatus.get(PaymentStatus.FAILED));
        assertNull(countByStatus.get(PaymentStatus.COMPLETED));
    }

    // 这个辅助函数负责持久化一个可用用户，避免每个测试重复构造账户基础数据。
    private User persistUser(String accountNumber) {
        User user = new User();
        user.setAccountNumber(accountNumber);
        user.setBalance(new BigDecimal("1000.00"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPaymentPasswordHash("pwd-123");
        return entityManager.persistAndFlush(user);
    }

    // 这个辅助函数负责持久化支付记录，统一设置仓储查询所需的关键字段。
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
}
