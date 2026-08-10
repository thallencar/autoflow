package br.com.autoflow.application.dto;

import java.math.BigDecimal;

public record EstoqueRequest(
        String nomeItem,
        String nomeMarca,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque,
        Integer quantidadeMinima,
        String tipoCategoria
) {}