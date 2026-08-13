package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrcamentoRequest(
        String tipoOrcamento,
        LocalDateTime dataExpiracao,
        BigDecimal subtotalPecas,
        BigDecimal maoObra,
        BigDecimal total,
        List<OrcamentoItemRequest> itens
) {}