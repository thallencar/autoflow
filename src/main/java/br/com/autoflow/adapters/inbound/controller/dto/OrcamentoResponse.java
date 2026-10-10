package br.com.autoflow.adapters.inbound.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.autoflow.domain.enums.StatusOrcamento;

public record OrcamentoResponse(
        UUID id,
        UUID idOs,
        String tipoOrcamento,
        StatusOrcamento status,
        LocalDateTime dataCriacao,
        LocalDateTime dataExpiracao,
        LocalDateTime dataDecisao,
        BigDecimal subtotalPecas,
        BigDecimal maoObra,
        BigDecimal total,
        List<OrcamentoServicoResponse> servicos,
        List<String> avisosEstoque
) {}