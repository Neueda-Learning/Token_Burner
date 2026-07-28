package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Payment entity persistence and lookup operations.
 * Provides methods for storing and querying payment records.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    interface PaymentStatusCountProjection {
        PaymentStatus getStatus();

        Long getTotal();
    }

    /**
     * Find all payments related to a specific user.
     * A payment belongs to a user when the user is either the source account or the destination account.
     *
     * @param sourceAccountId the ID of the user as source account
     * @param destinationAccountId the ID of the user as destination account
     * @return a list of payments where the user is either source or destination
     */
    List<Payment> findBySourceAccount_IdOrDestinationAccountId(
            Long sourceAccountId,
            Long destinationAccountId
    );

    long countByStatus(PaymentStatus status);

    @Query("select sum(p.amount) from Payment p")
    BigDecimal sumAllAmounts();

    @Query("select p.status as status, count(p) as total from Payment p group by p.status")
    List<PaymentStatusCountProjection> countPaymentsGroupedByStatus();
}