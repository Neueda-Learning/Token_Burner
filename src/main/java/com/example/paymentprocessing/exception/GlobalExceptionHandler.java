package com.example.paymentprocessing.exception;

import com.example.paymentprocessing.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Centralized API exception handling for business exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("PAYMENT_NOT_FOUND", exception.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("USER_NOT_FOUND", exception.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InvalidPaymentPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentPassword(
            InvalidPaymentPasswordException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("INVALID_PAYMENT_PASSWORD", exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidPaymentAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentAmount(
            InvalidPaymentAmountException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("INVALID_PAYMENT_AMOUNT", exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStatus(
            InvalidPaymentStatusException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("INVALID_PAYMENT_STATUS", exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidAccountStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAccountStatus(
            InvalidAccountStatusException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("INVALID_ACCOUNT_STATUS", exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse("INSUFFICIENT_BALANCE", exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String errorCode,
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode,
                message,
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}

