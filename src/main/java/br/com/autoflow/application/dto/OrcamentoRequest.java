package br.com.autoflow.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrcamentoRequest(
        @NotNull(message = "O ID da Ordem de Serviço é obrigatório")
        UUID idOs,

        @NotNull(message = "O tipo do orçamento é obrigatório")
        String tipoOrcamento,
        @NotNull(message = "A data de expiração é obrigatória")
        LocalDateTime dataExpiracao,

        @NotNull(message = "O valor subtotal das peças é obrigatório")
        @PositiveOrZero(message = "O valor da mão de obra não pode ser negativo")
        BigDecimal subtotalPecas,

        @NotNull(message = "O valor da mão de obra é obrigatório")
        @PositiveOrZero(message = "O valor da mão de obra não pode ser negativo")
        BigDecimal maoObra,

        @NotNull(message = "O valor total do orçamento é obrigatório")
        @PositiveOrZero(message = "O valor da mão de obra não pode ser negativo")
        BigDecimal total,

        @NotEmpty(message = "O orçamento deve conter pelo menos um item")
        @Valid
        List<OrcamentoItemRequest> itens
) {}