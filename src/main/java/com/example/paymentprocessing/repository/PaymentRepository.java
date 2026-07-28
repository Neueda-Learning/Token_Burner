package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Payment entity persistence and lookup operations.
 * Provides methods for storing and querying payment records.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

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
}