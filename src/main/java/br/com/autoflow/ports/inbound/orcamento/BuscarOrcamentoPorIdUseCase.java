package br.com.autoflow.ports.inbound.orcamento;

import br.com.autoflow.domain.model.Orcamento;

import java.util.UUID;

public interface BuscarOrcamentoPorIdUseCase {
    Orcamento buscarPorId(UUID id);
}
