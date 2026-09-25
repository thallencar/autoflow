package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.domain.model.Veiculo;

public interface BuscarVeiculoPorPlacaUseCase {
    Veiculo buscarPorPlaca(String placa);
}
