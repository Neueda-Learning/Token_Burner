package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for User entity persistence and lookup operations.
 * Provides methods for retrieving user information required for payment verification
 * and accessing stored payment password hash when verification is required.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their account number.
     *
     * Needed to resolve a destination account number back into a User,
     * both when validating the destination user on createPayment and when
     * mapping Payment.destinationAccountNumber back to
     * PaymentResponse.destinationAccountId.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the User if found, or empty Optional if not found
     */
    Optional<User> findByAccountNumber(String accountNumber);
}



