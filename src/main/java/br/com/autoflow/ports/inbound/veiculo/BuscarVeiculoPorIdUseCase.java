package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.domain.model.Veiculo;

import java.util.UUID;

public interface BuscarVeiculoPorIdUseCase {
    Veiculo buscarPorId(UUID id);
}