package br.com.autoflow.adapters.inbound.controller.dto;

public record TokenResponse(
        String token,
        String tipo
) {
    public TokenResponse(String token) {
        this(token, "Bearer");
    }
}
