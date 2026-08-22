package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusPagamento;

public record AtualizarStatusPagamentoRequest(
        StatusPagamento stPagamento
) {
}
