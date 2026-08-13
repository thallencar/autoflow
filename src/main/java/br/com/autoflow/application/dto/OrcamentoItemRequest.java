package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrcamentoItemRequest(
    Integer quantidade,
    BigDecimal valorUnitario,
    BigDecimal valorTotal,
    UUID idEstoque
) {}