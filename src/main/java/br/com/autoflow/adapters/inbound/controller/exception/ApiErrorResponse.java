package br.com.autoflow.adapters.inbound.controller.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String message
) {
}