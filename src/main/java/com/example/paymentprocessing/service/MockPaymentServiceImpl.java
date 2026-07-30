package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import com.example.paymentprocessing.exception.InsufficientBalanceException;
import com.example.paymentprocessing.exception.InvalidCurrencyException;
import com.example.paymentprocessing.exception.InvalidPaymentAmountException;
import com.example.paymentprocessing.exception.InvalidPaymentPasswordException;
import com.example.paymentprocessing.exception.InvalidPaymentStatusException;
import com.example.paymentprocessing.exception.PaymentNotFoundException;
import com.example.paymentprocessing.exception.UserNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory mock implementation of the FROZEN {@link PaymentService} contract
 * (docs/service-contract.md). Active only under the "mock" Spring profile, so the
 * controller layer (Leon) can implement and test the controller/DTO/exception-handling
 * layer end-to-end without a running MySQL database and without depending on the real,
 * database-backed {@link PaymentServiceImpl}.
 * <p>
 * See docs/mock-service-guide.md for how to enable this profile and the exact mock data
 * and trigger conditions used to exercise each documented exception.
 * </p>
 */
@Service
@Profile("mock")
public class MockPaymentServiceImpl implements PaymentService {

    private static final BigDecimal SINGLE_PAYMENT_LIMIT = new BigDecimal("1000000.00");

    private static final String INACTIVE_ACCOUNT_FAILURE_NOTE =
            "Payment failed: source or destination account is inactive";

    private static final String SINGLE_PAYMENT_LIMIT_FAILURE_NOTE =
            "Payment failed: transaction amount exceeds the single-payment limit";

    // Fixed mock user IDs treated as known/valid. Any other ID triggers UserNotFoundException.
    private static final long KNOWN_USER_1 = 1001L;
    private static final long KNOWN_USER_2 = 1002L;
    private static final long KNOWN_USER_3 = 1003L;

    // Reserved mock user ID treated as an inactive account. Used to trigger InvalidAccountStatusException.
    private static final long INACTIVE_USER = 99L;

    // Fixed mock payment password. Any other value triggers InvalidPaymentPasswordException.
    private static final String MOCK_PAYMENT_PASSWORD = "888888";

    // Fixed mock balance cap shared by every known user. Used to trigger InsufficientBalanceException.
    private static final BigDecimal MOCK_BALANCE_LIMIT = new BigDecimal("100000.00");

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP");

    private static final EnumSet<PaymentStatus> TERMINAL_STATUSES = EnumSet.of(
            PaymentStatus.COMPLETED,
            PaymentStatus.FAILED
    );

    private final Map<Long, PaymentResponse> payments = new ConcurrentHashMap<>();
    private final Map<Long, List<PaymentHistoryResponse>> historyByPaymentId = new ConcurrentHashMap<>();
    private final AtomicLong paymentIdSequence = new AtomicLong(102);
    private final AtomicLong historyIdSequence = new AtomicLong(1);

    public MockPaymentServiceImpl() {
        seedMockData();
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        assertKnownUser(request.sourceAccountId());
        assertKnownUser(request.destinationAccountId());

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException();
        }

        String normalizedCurrency = normalizeAndValidateCurrency(request.currency());

        if (!Objects.equals(request.paymentPassword(), MOCK_PAYMENT_PASSWORD)) {
            throw new InvalidPaymentPasswordException();
        }

        boolean accountsActive = request.sourceAccountId() != INACTIVE_USER && request.destinationAccountId() != INACTIVE_USER;
        boolean withinSinglePaymentLimit = request.amount().compareTo(SINGLE_PAYMENT_LIMIT) <= 0;

        if (accountsActive && withinSinglePaymentLimit && request.amount().compareTo(MOCK_BALANCE_LIMIT) > 0) {
            throw new InsufficientBalanceException();
        }

        Long paymentId = paymentIdSequence.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        PaymentResponse payment = new PaymentResponse(
                paymentId,
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                normalizedCurrency,
                PaymentStatus.CREATED,
                now,
                now
        );

        payments.put(paymentId, payment);
        appendHistory(paymentId, null, PaymentStatus.CREATED, buildTransitionNotes(PaymentStatus.CREATED));

        if (!accountsActive) {
            return persistTransition(payment, PaymentStatus.FAILED, INACTIVE_ACCOUNT_FAILURE_NOTE);
        }

        if (!withinSinglePaymentLimit) {
            return persistTransition(payment, PaymentStatus.FAILED, SINGLE_PAYMENT_LIMIT_FAILURE_NOTE);
        }

        return payment;
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        return findPaymentOrThrow(paymentId);
    }

    @Override
    public List<PaymentHistoryResponse> getPaymentHistory(Long paymentId) {
        findPaymentOrThrow(paymentId);
        return historyByPaymentId.getOrDefault(paymentId, List.of());
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        assertKnownUser(userId);

        return payments.values().stream()
                .filter(payment -> Objects.equals(payment.sourceAccountId(), userId)
                        || Objects.equals(payment.destinationAccountId(), userId))
                .toList();
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request) {
        PaymentResponse payment = findPaymentOrThrow(paymentId);

        PaymentStatus currentStatus = payment.status();
        PaymentStatus targetStatus = request.status();

        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new InvalidPaymentStatusException(buildInvalidTransitionMessage(currentStatus, targetStatus));
        }

        return persistTransition(payment, targetStatus, buildTransitionNotes(targetStatus));
    }

    private void assertKnownUser(Long userId) {
        if (userId == null
                || (userId != KNOWN_USER_1 && userId != KNOWN_USER_2 && userId != KNOWN_USER_3 && userId != INACTIVE_USER)) {
            throw new UserNotFoundException(userId);
        }
    }

    private PaymentResponse findPaymentOrThrow(Long paymentId) {
        PaymentResponse payment = payments.get(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException(paymentId);
        }
        return payment;
    }

    private void appendHistory(Long paymentId, PaymentStatus previousStatus, PaymentStatus newStatus, String notes) {
        PaymentHistoryResponse historyEntry = new PaymentHistoryResponse(
                historyIdSequence.getAndIncrement(),
                paymentId,
                previousStatus,
                newStatus,
                LocalDateTime.now(),
                notes
        );

        historyByPaymentId.merge(
                paymentId,
                List.of(historyEntry),
                (existing, added) -> {
                    List<PaymentHistoryResponse> merged = new java.util.ArrayList<>(existing);
                    merged.addAll(added);
                    return List.copyOf(merged);
                }
        );
    }

    private PaymentResponse persistTransition(PaymentResponse payment, PaymentStatus targetStatus, String notes) {
        PaymentResponse updatedPayment = new PaymentResponse(
                payment.paymentId(),
                payment.sourceAccountId(),
                payment.destinationAccountId(),
                payment.amount(),
                payment.currency(),
                targetStatus,
                payment.createdAt(),
                LocalDateTime.now()
        );

        payments.put(payment.paymentId(), updatedPayment);
        appendHistory(payment.paymentId(), payment.status(), targetStatus, notes);

        return updatedPayment;
    }

    // Mirrors the frozen transition rules in PaymentServiceImpl.isTransitionAllowed
    // (docs/service-contract.md section 3). Keep both copies in sync if the rules ever change.
    private boolean isTransitionAllowed(PaymentStatus currentStatus, PaymentStatus targetStatus) {
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

    private String buildInvalidTransitionMessage(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        return "Cannot change payment status from " + currentStatus + " to " + targetStatus;
    }

    private String buildTransitionNotes(PaymentStatus newStatus) {
        return switch (newStatus) {
            case CREATED -> "Payment created successfully";
            case VALIDATED -> "Payment request validated";
            case SENT -> "Payment sent for processing";
            case COMPLETED -> "Payment completed successfully";
            case FAILED -> "Payment marked as failed";
        };
    }

    private String normalizeAndValidateCurrency(String currency) {
        if (currency == null) {
            throw new InvalidCurrencyException();
        }

        String normalizedCurrency = currency.trim().toUpperCase();
        if (!SUPPORTED_CURRENCIES.contains(normalizedCurrency)) {
            throw new InvalidCurrencyException();
        }

        return normalizedCurrency;
    }

    // Seeds two sample payments aligned with the existing frontend integration data so GET
    // endpoints return compatible results immediately without any createPayment call first.
    private void seedMockData() {
        Long firstPaymentId = 101L;
        LocalDateTime firstCreatedAt = LocalDateTime.now().minusHours(2);
        payments.put(firstPaymentId, new PaymentResponse(
                firstPaymentId,
                KNOWN_USER_1,
                KNOWN_USER_2,
                new BigDecimal("1500.00"),
                "USD",
                PaymentStatus.COMPLETED,
                firstCreatedAt,
                firstCreatedAt.plusMinutes(10)
        ));
        appendHistory(firstPaymentId, null, PaymentStatus.CREATED, buildTransitionNotes(PaymentStatus.CREATED));
        appendHistory(firstPaymentId, PaymentStatus.CREATED, PaymentStatus.VALIDATED, buildTransitionNotes(PaymentStatus.VALIDATED));
        appendHistory(firstPaymentId, PaymentStatus.VALIDATED, PaymentStatus.SENT, buildTransitionNotes(PaymentStatus.SENT));
        appendHistory(firstPaymentId, PaymentStatus.SENT, PaymentStatus.COMPLETED, buildTransitionNotes(PaymentStatus.COMPLETED));

        Long secondPaymentId = 102L;
        LocalDateTime secondCreatedAt = LocalDateTime.now().minusHours(1);
        payments.put(secondPaymentId, new PaymentResponse(
                secondPaymentId,
                KNOWN_USER_3,
                KNOWN_USER_1,
                new BigDecimal("275.50"),
                "USD",
                PaymentStatus.SENT,
                secondCreatedAt,
                secondCreatedAt.plusMinutes(7)
        ));
        appendHistory(secondPaymentId, null, PaymentStatus.CREATED, buildTransitionNotes(PaymentStatus.CREATED));
        appendHistory(secondPaymentId, PaymentStatus.CREATED, PaymentStatus.VALIDATED, buildTransitionNotes(PaymentStatus.VALIDATED));
        appendHistory(secondPaymentId, PaymentStatus.VALIDATED, PaymentStatus.SENT, buildTransitionNotes(PaymentStatus.SENT));
    }
}
