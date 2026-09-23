package br.com.autoflow.ports.inbound.veiculo;

import java.util.UUID;

public interface DeletarVeiculoUseCase {
    void deletar(UUID id);
}
