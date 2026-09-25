package br.com.autoflow.ports.inbound.servico;

import br.com.autoflow.domain.model.Servico;

import java.util.UUID;

public interface AtualizarServicoUseCase {
    Servico atualizar(UUID id, Servico servico);
}
