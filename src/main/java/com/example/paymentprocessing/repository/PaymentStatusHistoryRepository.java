package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for PaymentStatusHistory entity persistence and lookup operations.
 * Provides methods for storing and querying payment status history records
 * to track payment lifecycle changes.
 */
public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {

    /**
     * Find all status history records for a specific payment, ordered by time ascending.
     *
     * The service contract requires the result to be sorted by changedAt ascending.
     *
     * @param paymentId the ID of the payment
     * @return a list of PaymentStatusHistory records for the payment, sorted by changedAt ascending
     */
    List<PaymentStatusHistory> findByPayment_IdOrderByChangedAtAsc(Long paymentId);
}


