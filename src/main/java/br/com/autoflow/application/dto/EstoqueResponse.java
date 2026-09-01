package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.TipoItemEstoque;

import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueResponse(
        UUID id,
        String nomeItem,
        String nomeMarca,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque,
        Integer quantidadeMinima,
        TipoItemEstoque tipoCategoria
) {}