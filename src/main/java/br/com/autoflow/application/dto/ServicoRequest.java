package br.com.autoflow.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank(message = "A descrição do serviço é obrigatória")
        String dsServico,

        @NotNull(message = "O valor do serviço é obrigatório")
        @Positive(message = "O valor do serviço deve ser maior que zero")
        BigDecimal vlServico,

        @NotNull(message = "O tempo estimado em minutos é obrigatório")
        @Positive(message = "O tempo estimado deve ser maior que zero")
        Integer qtTempoEstimadoMin
) {}