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

        @NotEmpty(message = "O orçamento deve conter pelo menos um serviço")
        @Valid
        List<OrcamentoServicoRequest> servicos,

        @Valid
        List<OrcamentoItemRequest> itens
) {}