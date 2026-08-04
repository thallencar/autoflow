package br.com.autoflow.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String message
) {
}