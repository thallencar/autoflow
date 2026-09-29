package br.com.autoflow.ports.inbound.ordemservico;

import br.com.autoflow.domain.model.OrdemServico;

public interface CriarOrdemServicoUseCase {
    OrdemServico criar(OrdemServico request, boolean possuiAgendamento);
}
