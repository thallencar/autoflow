package br.com.autoflow.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "O login é obrigatorio")
        String login,

        @NotBlank(message = "Asenha é obrigatória")
        String senha
) {}
