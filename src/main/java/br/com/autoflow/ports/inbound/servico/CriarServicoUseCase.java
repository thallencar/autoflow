package br.com.autoflow.ports.inbound.servico;

import br.com.autoflow.domain.model.Servico;

public interface CriarServicoUseCase {
    Servico criar(Servico servico);
}
