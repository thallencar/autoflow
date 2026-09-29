package br.com.autoflow.ports.inbound.ordemservico;

import java.util.UUID;

public interface DeletarOrdemServicoUseCase {
    void deletar(UUID id);
}
