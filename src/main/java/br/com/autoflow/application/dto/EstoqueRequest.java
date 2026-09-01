package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.TipoItemEstoque;

import java.math.BigDecimal;

public record EstoqueRequest(
        String nomeItem,
        String nomeMarca,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque,
        Integer quantidadeMinima,
        TipoItemEstoque tipoCategoria
) {}