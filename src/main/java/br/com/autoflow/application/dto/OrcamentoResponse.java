package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrcamentoResponse(
        UUID id,
        UUID idOs,
        String tipoOrcamento,
        String status,
        LocalDateTime dataCriacao,
        LocalDateTime dataExpiracao,
        LocalDateTime dataDecisao,
        BigDecimal subtotalPecas,
        BigDecimal maoObra,
        BigDecimal total,
        List<OrcamentoServicoResponse> servicos,
        List<String> avisosEstoque
) {}