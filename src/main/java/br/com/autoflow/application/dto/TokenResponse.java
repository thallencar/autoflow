package br.com.autoflow.application.dto;

public record TokenResponse(
        String token,
        String tipo
) {
    public TokenResponse(String token) {
        this(token, "Bearer");
    }
}
