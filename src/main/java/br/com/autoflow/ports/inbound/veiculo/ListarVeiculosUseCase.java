package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.domain.model.Veiculo;

import java.util.List;

public interface ListarVeiculosUseCase {
    List<Veiculo> listar();
}
