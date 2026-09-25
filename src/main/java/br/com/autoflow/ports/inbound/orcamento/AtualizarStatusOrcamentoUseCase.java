package br.com.autoflow.ports.inbound.orcamento;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.model.Orcamento;

import java.util.UUID;

public interface AtualizarStatusOrcamentoUseCase {
    Orcamento atualizarStatus(UUID id, StatusOrcamento novoStatus);
}
