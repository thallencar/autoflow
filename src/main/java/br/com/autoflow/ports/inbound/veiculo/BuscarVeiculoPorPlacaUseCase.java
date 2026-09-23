package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;

public interface BuscarVeiculoPorPlacaUseCase {
    VeiculoResponse buscarPorPlaca(String placa);
}
