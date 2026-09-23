package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;

import java.util.UUID;

public interface BuscarVeiculoPorIdUseCase {
    VeiculoResponse buscarPorId(UUID id);
}