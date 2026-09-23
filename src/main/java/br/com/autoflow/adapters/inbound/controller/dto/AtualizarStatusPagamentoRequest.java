package br.com.autoflow.adapters.inbound.controller.dto;

import br.com.autoflow.domain.enums.StatusPagamento;

public record AtualizarStatusPagamentoRequest(
        StatusPagamento stPagamento
) {
}
