package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;

import java.util.List;

/**
 * Fixed service contract defined in docs/service-contract.md.
 * Method names, parameters, and return types must not change without team discussion.
 */
public interface PaymentService {

    /**
     * Endpoint: POST /api/payments
     * Responsibilities: validate the request, verify the source/destination users exist,
     * verify the payment password against the stored hash, check account status and balance,
     * create the Payment record, create the initial PaymentStatusHistory record, and return PaymentResponse.
     * Possible exceptions: UserNotFoundException, InvalidPaymentPasswordException,
     * InsufficientBalanceException, InvalidPaymentAmountException, InvalidAccountStatusException.
     */
    PaymentResponse createPayment(CreatePaymentRequest request);

    /**
     * Endpoint: GET /api/payments/{paymentId}
     * Responsibilities: find the payment by ID and convert the entity to PaymentResponse.
     * Possible exceptions: PaymentNotFoundException.
     */
    PaymentResponse getPaymentById(Long paymentId);

    /**
     * Endpoint: GET /api/payments/{paymentId}/history
     * Responsibilities: verify the payment exists, retrieve its status history sorted by
     * changedAt ascending, and convert each entity to PaymentHistoryResponse.
     * Possible exceptions: PaymentNotFoundException.
     */
    List<PaymentHistoryResponse> getPaymentHistory(Long paymentId);

    /**
     * Endpoint: GET /api/payments/user/{userId}
     * Responsibilities: return every payment where sourceAccountId or destinationAccountId
     * equals userId. Return an empty list when the user exists but has no payments.
     * Possible exceptions: UserNotFoundException (if user existence verification is implemented).
     */
    List<PaymentResponse> getPaymentsByUser(Long userId);

    /**
     * Endpoint: PUT /api/payments/{paymentId}/status
     * Responsibilities: find the payment, validate the requested status transition,
     * update the payment status, create a PaymentStatusHistory record, and return PaymentResponse.
     * Possible exceptions: PaymentNotFoundException, InvalidPaymentStatusException.
     */
    PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);
}

