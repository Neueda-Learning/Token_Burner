package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.entity.Payment;
import com.example.paymentprocessing.entity.PaymentStatusHistory;
import com.example.paymentprocessing.entity.User;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.enums.UserStatus;
import com.example.paymentprocessing.exception.InsufficientBalanceException;
import com.example.paymentprocessing.exception.InvalidAccountStatusException;
import com.example.paymentprocessing.exception.InvalidPaymentAmountException;
import com.example.paymentprocessing.exception.InvalidPaymentPasswordException;
import com.example.paymentprocessing.exception.InvalidPaymentStatusException;
import com.example.paymentprocessing.exception.PaymentNotFoundException;
import com.example.paymentprocessing.exception.UserNotFoundException;
import com.example.paymentprocessing.repository.PaymentRepository;
import com.example.paymentprocessing.repository.PaymentStatusHistoryRepository;
import com.example.paymentprocessing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        User sourceUser = userRepository.findById(request.sourceAccountId())
                .orElseThrow(() -> new UserNotFoundException(request.sourceAccountId()));
        User destinationUser = userRepository.findById(request.destinationAccountId())
                .orElseThrow(() -> new UserNotFoundException(request.destinationAccountId()));

        if (sourceUser.getStatus() != UserStatus.ACTIVE || destinationUser.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidAccountStatusException();
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException();
        }

        // No PasswordEncoder is configured in this training project, so the payment password
        // is compared directly against the stored hash, per docs/service-contract.md section 2.1.
        if (!Objects.equals(request.paymentPassword(), sourceUser.getPaymentPasswordHash())) {
            throw new InvalidPaymentPasswordException();
        }

        if (sourceUser.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }

        Payment payment = new Payment();
        payment.setSourceAccount(sourceUser);
        payment.setDestinationAccountId(request.destinationAccountId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        Payment savedPayment = paymentRepository.save(payment);

        PaymentStatusHistory history = buildHistoryRecord(
                savedPayment,
                null,
                savedPayment.getStatus(),
                buildTransitionNotes(savedPayment.getStatus())
        );
        paymentStatusHistoryRepository.save(history);

        return mapToPaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new PaymentNotFoundException(paymentId);
        }

        return paymentStatusHistoryRepository.findByPayment_IdOrderByChangedAtAsc(paymentId).stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return paymentRepository.findBySourceAccount_IdOrDestinationAccountId(userId, userId).stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        PaymentStatus currentStatus = payment.getStatus();
        PaymentStatus targetStatus = request.status();

        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new InvalidPaymentStatusException(buildInvalidTransitionMessage(currentStatus, targetStatus));
        }

        payment.setStatus(targetStatus);
        Payment updatedPayment = paymentRepository.save(payment);

        PaymentStatusHistory history = buildHistoryRecord(
                updatedPayment,
                currentStatus,
                targetStatus,
                buildTransitionNotes(targetStatus)
        );
        paymentStatusHistoryRepository.save(history);

        return mapToPaymentResponse(updatedPayment);
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

    // Converts a Payment entity into the DTO exposed to the Controller layer.
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getSourceAccount().getId(),
                payment.getDestinationAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    // Converts a PaymentStatusHistory entity into the DTO exposed to the Controller layer.
    private PaymentHistoryResponse mapToHistoryResponse(PaymentStatusHistory history) {
        return new PaymentHistoryResponse(
                history.getId(),
                history.getPayment().getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt(),
                history.getNotes()
        );
    }
}
