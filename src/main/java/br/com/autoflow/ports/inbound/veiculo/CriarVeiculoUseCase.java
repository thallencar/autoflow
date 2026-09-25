package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.domain.model.Veiculo;

public interface CriarVeiculoUseCase {
    Veiculo criar(Veiculo request);
}
