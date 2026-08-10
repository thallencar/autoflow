package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueResponse(
        UUID id,
        String nomeItem,
        String nomeMarca,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque,
        Integer quantidadeMinima,
        String tipoCategoria
) {}