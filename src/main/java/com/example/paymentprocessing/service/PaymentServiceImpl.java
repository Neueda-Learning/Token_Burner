package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.PaymentStatusHistoryRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the fixed contract in docs/service-contract.md.
 * Uses constructor injection as required by the team rule.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final EnumSet<PaymentStatus> TERMINAL_STATUSES = EnumSet.of(
            PaymentStatus.COMPLETED,
            PaymentStatus.FAILED
    );

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentStatusHistoryRepository paymentStatusHistoryRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentStatusHistoryRepository = paymentStatusHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Blocked dependencies (per docs/service-contract.md section 2.1):
        // 1. CreatePaymentRequest still has no fields (sourceAccountId, destinationAccountId,
        //    amount, currency, paymentPassword) or getters.
        // 2. PaymentResponse still has no fields to map to.
        // 3. PaymentRepository/UserRepository do not expose save/find methods yet.
        // 4. UserNotFoundException, InvalidPaymentPasswordException, InsufficientBalanceException,
        //    InvalidPaymentAmountException, InvalidAccountStatusException are still empty placeholder classes.
        // 5. Payment entity currently models destinationAccountNumber instead of destinationAccountId,
        //    which does not match the SQL/API design referenced by this contract.
        //
        // Planned implementation once dependencies are ready:
        // 1. Load the source user and destination user; throw UserNotFoundException if missing.
        // 2. Verify request.paymentPassword() against sourceUser.getPaymentPasswordHash();
        //    throw InvalidPaymentPasswordException on mismatch.
        // 3. Validate account status (InvalidAccountStatusException) and amount (InvalidPaymentAmountException),
        //    and check balance if required (InsufficientBalanceException).
        // 4. Build a Payment entity with status = CREATED and persist it.
        // 5. Persist the first PaymentStatusHistory row with previousStatus = null, newStatus = CREATED.
        // 6. Map the saved Payment entity to PaymentResponse and return it.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        // Blocked dependencies (per docs/service-contract.md section 2.2):
        // 1. PaymentResponse is still an empty placeholder.
        // 2. PaymentRepository does not define findById yet.
        // 3. PaymentNotFoundException is still an empty placeholder class.
        //
        // Planned implementation once dependencies are ready:
        // 1. Load the payment by ID.
        // 2. Throw PaymentNotFoundException when the payment does not exist.
        // 3. Map the entity to PaymentResponse and return it.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long paymentId) {
        // Blocked dependencies (per docs/service-contract.md section 2.3):
        // 1. PaymentHistoryResponse is still an empty placeholder.
        // 2. PaymentRepository does not define findById yet.
        // 3. PaymentStatusHistoryRepository does not define a history lookup method yet.
        // 4. PaymentNotFoundException is still an empty placeholder class.
        //
        // Planned implementation once dependencies are ready:
        // 1. Verify the payment exists; throw PaymentNotFoundException if not.
        // 2. Load all PaymentStatusHistory rows for this payment, sorted by changedAt ascending.
        // 3. Map each entity to a PaymentHistoryResponse and return the list.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        // Blocked dependencies (per docs/service-contract.md section 2.4):
        // 1. PaymentResponse is still an empty placeholder.
        // 2. UserRepository does not define an existence-check method yet.
        // 3. PaymentRepository does not define a query by source/destination account yet.
        // 4. Payment entity cannot currently represent destinationAccountId as designed.
        //
        // Planned implementation once dependencies are ready:
        // 1. Optionally verify the user exists; throw UserNotFoundException if user verification is implemented.
        // 2. Load all payments where sourceAccountId = userId or destinationAccountId = userId.
        // 3. Return an empty list when the user exists but has no payments (do not throw in that case).
        // 4. Map each entity to PaymentResponse and return the list.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request) {
        // Blocked dependencies (per docs/service-contract.md section 2.5):
        // 1. UpdatePaymentStatusRequest still has no status field or getter.
        // 2. PaymentResponse is still an empty placeholder.
        // 3. PaymentRepository/PaymentStatusHistoryRepository do not define persistence methods yet.
        // 4. PaymentNotFoundException and InvalidPaymentStatusException are still empty placeholder classes.
        //
        // Planned implementation once dependencies are ready:
        // 1. Load the payment by ID; throw PaymentNotFoundException if missing.
        // 2. Validate the requested transition using isTransitionAllowed(...);
        //    throw InvalidPaymentStatusException with buildInvalidTransitionMessage(...) if invalid.
        // 3. Update the payment status.
        // 4. Persist a PaymentStatusHistory row built with buildHistoryRecord(...).
        // 5. Map the updated payment to PaymentResponse and return it.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Centralizes the frozen transition rules from docs/api-design.md and docs/service-contract.md:
     * CREATED -> VALIDATED, CREATED -> FAILED, VALIDATED -> SENT, VALIDATED -> FAILED,
     * SENT -> COMPLETED, SENT -> FAILED. COMPLETED and FAILED are terminal states.
     */
    boolean isTransitionAllowed(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (Objects.isNull(currentStatus) || Objects.isNull(targetStatus) || currentStatus == targetStatus) {
            return false;
        }

        if (TERMINAL_STATUSES.contains(currentStatus)) {
            return false;
        }

        return switch (currentStatus) {
            case CREATED -> targetStatus == PaymentStatus.VALIDATED || targetStatus == PaymentStatus.FAILED;
            case VALIDATED -> targetStatus == PaymentStatus.SENT || targetStatus == PaymentStatus.FAILED;
            case SENT -> targetStatus == PaymentStatus.COMPLETED || targetStatus == PaymentStatus.FAILED;
            case COMPLETED, FAILED -> false;
        };
    }

    // Builds a consistent message for InvalidPaymentStatusException.
    String buildInvalidTransitionMessage(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        return "Cannot change payment status from " + currentStatus + " to " + targetStatus;
    }

    // Provides default audit notes for successful lifecycle changes.
    String buildTransitionNotes(PaymentStatus newStatus) {
        return switch (newStatus) {
            case CREATED -> "Payment created successfully";
            case VALIDATED -> "Payment request validated";
            case SENT -> "Payment sent for processing";
            case COMPLETED -> "Payment completed successfully";
            case FAILED -> "Payment marked as failed";
        };
    }

    // Creates a history entity so the public methods can stay focused on orchestration.
    PaymentStatusHistory buildHistoryRecord(
            Payment payment,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            String notes
    ) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPayment(payment);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setNotes(notes);
        return history;
    }
}
