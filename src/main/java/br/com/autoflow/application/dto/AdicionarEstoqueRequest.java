package br.com.autoflow.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdicionarEstoqueRequest(
        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade a adicionar deve ser no mínimo 1")
        Integer quantidade
) {}