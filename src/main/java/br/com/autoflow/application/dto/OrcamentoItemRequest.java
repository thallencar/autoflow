package br.com.autoflow.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record OrcamentoItemRequest(
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @NotNull(message = "O valor unitário é obrigatório")
        @PositiveOrZero(message = "O valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @NotNull(message = "O valor total do item é obrigatório")
        @PositiveOrZero(message = "O valor total do item não pode ser negativo")
        BigDecimal valorTotal,

        @NotNull(message = "O ID do estoque é obrigatório")
        UUID idEstoque
) {}