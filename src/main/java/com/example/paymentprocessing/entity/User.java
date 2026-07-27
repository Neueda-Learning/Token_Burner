package com.example.paymentprocessing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * JPA entity representing the {@code users} table.
 * <p>
 * This model stores account-related user data, including only the hashed
 * payment password. Plain text payment passwords must never be persisted.
 * Password verification and hashing behavior belong to later service and
 * security work, not to this entity.
 * </p>
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "payment_password_hash", nullable = false)
    private String paymentPasswordHash;

    public User() {
    }

    public User(Long id, String accountNumber, BigDecimal balance, String status, String paymentPasswordHash) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.paymentPasswordHash = paymentPasswordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentPasswordHash() {
        return paymentPasswordHash;
    }

    public void setPaymentPasswordHash(String paymentPasswordHash) {
        this.paymentPasswordHash = paymentPasswordHash;
    }
}
