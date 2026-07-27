package com.example.paymentprocessing.controller;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.response.ErrorResponse;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;
import com.example.paymentprocessing.enums.PaymentStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * TODO: This controller currently uses temporary in-memory data only for
 * frontend-backend integration. Replace all map access with PaymentService
 * calls when the service and persistence layers are ready.
 * TODO Leon:
 * Define REST endpoints for payment-related API operations.
 * Add request mapping annotations, dependency wiring, and endpoint methods later.
 * Future responsibilities:
 * - Receive payment creation requests.
 * - Accept payment password input through the request DTO.
 * - Forward the request to the service layer.
 * Design rule:
 * - The controller must not verify the payment password directly.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";

    // TODO Replace temporary in-memory storage with PaymentService and persistence access later.
    private final Map<Long, PaymentResponse> paymentStore = new ConcurrentHashMap<>();

    // TODO Replace temporary in-memory history with service-driven payment status history retrieval later.
    private final Map<Long, List<PaymentHistoryResponse>> paymentHistoryStore = new ConcurrentHashMap<>();

    private final AtomicLong paymentIdGenerator = new AtomicLong(103L);
    private final AtomicLong historyIdGenerator = new AtomicLong(1008L);

    public PaymentController() {
        initializeSampleData();
        System.out.println("PaymentController initialized with sample data.");
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId, HttpServletRequest request) {
        PaymentResponse payment = paymentStore.get(paymentId);
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildPaymentNotFoundError(paymentId, request.getRequestURI()));
        }

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{paymentId}/history")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long paymentId, HttpServletRequest request) {
        if (!paymentStore.containsKey(paymentId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildPaymentNotFoundError(paymentId, request.getRequestURI()));
        }

        List<PaymentHistoryResponse> history = paymentHistoryStore.getOrDefault(paymentId, List.of())
                .stream()
                .sorted(Comparator.comparing(PaymentHistoryResponse::changedAt))
                .toList();

        return ResponseEntity.ok(history);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId) {
        // TODO When UserService or UserRepository is available, verify whether the user exists
        // and convert missing users into UserNotFoundException instead of always returning an array.
        List<PaymentResponse> payments = paymentStore.values().stream()
                .filter(payment -> userId.equals(payment.sourceAccountId()) || userId.equals(payment.destinationAccountId()))
                .sorted(Comparator.comparing(PaymentResponse::paymentId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Long paymentId = paymentIdGenerator.incrementAndGet();

        // TODO Replace this temporary controller-side object creation with PaymentService.createPayment(request).
        // TODO PaymentService will later verify request.paymentPassword() against User.paymentPasswordHash.
        // TODO Never store, log, or return request.paymentPassword().
        PaymentResponse response = new PaymentResponse(
                paymentId,
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency(),
                PaymentStatus.CREATED,
                now,
                now
        );

        paymentStore.put(paymentId, response);
        paymentHistoryStore.put(paymentId, new ArrayList<>(List.of(
                new PaymentHistoryResponse(
                        historyIdGenerator.incrementAndGet(),
                        paymentId,
                        null,
                        PaymentStatus.CREATED,
                        now,
                        "Payment created in temporary in-memory controller"
                )
        )));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void initializeSampleData() {
        LocalDateTime payment101CreatedAt = LocalDateTime.of(2026, 7, 27, 10, 30);
        LocalDateTime payment101UpdatedAt = LocalDateTime.of(2026, 7, 27, 10, 40);
        LocalDateTime payment102CreatedAt = LocalDateTime.of(2026, 7, 27, 11, 5);
        LocalDateTime payment102UpdatedAt = LocalDateTime.of(2026, 7, 27, 11, 12);
        LocalDateTime payment103CreatedAt = LocalDateTime.of(2026, 7, 27, 12, 0);
        LocalDateTime payment103UpdatedAt = LocalDateTime.of(2026, 7, 27, 12, 8);

        PaymentResponse payment101 = new PaymentResponse(
                101L,
                1001L,
                1002L,
                new BigDecimal("1500.00"),
                "USD",
                PaymentStatus.COMPLETED,
                payment101CreatedAt,
                payment101UpdatedAt
        );
        PaymentResponse payment102 = new PaymentResponse(
                102L,
                1003L,
                1001L,
                new BigDecimal("275.50"),
                "USD",
                PaymentStatus.SENT,
                payment102CreatedAt,
                payment102UpdatedAt
        );
        PaymentResponse payment103 = new PaymentResponse(
                103L,
                2001L,
                2002L,
                new BigDecimal("999.99"),
                "EUR",
                PaymentStatus.FAILED,
                payment103CreatedAt,
                payment103UpdatedAt
        );

        paymentStore.put(payment101.paymentId(), payment101);
        paymentStore.put(payment102.paymentId(), payment102);
        paymentStore.put(payment103.paymentId(), payment103);

        paymentHistoryStore.put(101L, new ArrayList<>(List.of(
                new PaymentHistoryResponse(1001L, 101L, null, PaymentStatus.CREATED, payment101CreatedAt, "Payment created"),
                new PaymentHistoryResponse(1002L, 101L, PaymentStatus.CREATED, PaymentStatus.VALIDATED, LocalDateTime.of(2026, 7, 27, 10, 32), "Payment request validated"),
                new PaymentHistoryResponse(1003L, 101L, PaymentStatus.VALIDATED, PaymentStatus.SENT, LocalDateTime.of(2026, 7, 27, 10, 35), "Payment sent for processing"),
                new PaymentHistoryResponse(1004L, 101L, PaymentStatus.SENT, PaymentStatus.COMPLETED, payment101UpdatedAt, "Payment completed successfully")
        )));

        paymentHistoryStore.put(102L, new ArrayList<>(List.of(
                new PaymentHistoryResponse(1005L, 102L, null, PaymentStatus.CREATED, payment102CreatedAt, "Payment created"),
                new PaymentHistoryResponse(1006L, 102L, PaymentStatus.CREATED, PaymentStatus.VALIDATED, LocalDateTime.of(2026, 7, 27, 11, 8), "Payment request validated"),
                new PaymentHistoryResponse(1007L, 102L, PaymentStatus.VALIDATED, PaymentStatus.SENT, payment102UpdatedAt, "Payment sent for processing")
        )));

        paymentHistoryStore.put(103L, new ArrayList<>(List.of(
                new PaymentHistoryResponse(1008L, 103L, null, PaymentStatus.CREATED, payment103CreatedAt, "Payment created"),
                new PaymentHistoryResponse(1009L, 103L, PaymentStatus.CREATED, PaymentStatus.FAILED, payment103UpdatedAt, "Payment failed during processing")
        )));

        historyIdGenerator.set(1009L);
    }

    private ErrorResponse buildPaymentNotFoundError(Long paymentId, String path) {
        return new ErrorResponse(
                PAYMENT_NOT_FOUND,
                "Payment with id %d was not found.".formatted(paymentId),
                LocalDateTime.now(),
                path
        );
    }
}

