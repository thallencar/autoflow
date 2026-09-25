package br.com.autoflow.ports.inbound.servico;

import br.com.autoflow.domain.model.Servico;

import java.util.UUID;

public interface BuscarServicoPorIdUseCase {
    Servico buscarPorId(UUID id);
}
