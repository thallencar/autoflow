package br.com.autoflow.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AtualizarValorEstoqueRequest(
        @NotNull(message = "O valor unitário é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valorUnitario
) {}