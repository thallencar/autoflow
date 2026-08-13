package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrcamentoItemResponse(
        UUID id,
        String statusReserva,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal,
        UUID idEstoque,
        UUID idOrcamento
) {}