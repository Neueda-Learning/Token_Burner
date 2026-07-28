package com.example.paymentprocessing.dto.response;

import java.time.LocalDateTime;

/**
 * Standard error response DTO for API failures.
 */
public record ErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp,
        String path
) {
}
