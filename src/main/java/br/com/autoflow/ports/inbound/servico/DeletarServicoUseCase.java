package br.com.autoflow.ports.inbound.servico;

import java.util.UUID;

public interface DeletarServicoUseCase {
    void deletar(UUID id);
}
