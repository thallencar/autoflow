package br.com.autoflow.ports.inbound.estoque;

import br.com.autoflow.domain.model.Estoque;

import java.util.UUID;

public interface BuscarEstoquePorIdUseCase {
    Estoque buscarPorId(UUID id);
}
