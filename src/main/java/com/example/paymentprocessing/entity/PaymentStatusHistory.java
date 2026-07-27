package com.example.paymentprocessing.entity;

import com.example.paymentprocessing.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity representing the {@code payment_status_history} table.
 * <p>
 * Each row records one lifecycle transition for a payment. This entity is for
 * status history only and must not store sensitive information such as payment
 * passwords or password hashes.
 * </p>
 */
@Entity
@Table(name = "payment_status_history")
public class PaymentStatusHistory {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private PaymentStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    public PaymentStatusHistory() {
    }

    public PaymentStatusHistory(Long id,
                                Long paymentId,
                                PaymentStatus previousStatus,
                                PaymentStatus newStatus,
                                LocalDateTime changedAt,
                                String notes) {
        this.id = id;
        this.paymentId = paymentId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PaymentStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public PaymentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PaymentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
