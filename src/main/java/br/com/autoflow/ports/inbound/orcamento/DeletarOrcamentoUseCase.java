package br.com.autoflow.ports.inbound.orcamento;

import java.util.UUID;

public interface DeletarOrcamentoUseCase {
    void deletar(UUID id);
}
