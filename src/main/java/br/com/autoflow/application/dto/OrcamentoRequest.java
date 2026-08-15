package br.com.autoflow.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrcamentoRequest(
        @NotNull(message = "O ID da Ordem de Serviço é obrigatório")
        UUID idOs,
        String tipoOrcamento,
        LocalDateTime dataExpiracao,
        BigDecimal subtotalPecas,
        BigDecimal maoObra,
        BigDecimal total,
        List<OrcamentoItemRequest> itens
) {}