package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;

import java.util.List;

public interface ListarVeiculosUseCase {
    List<VeiculoResponse> listar();
}
