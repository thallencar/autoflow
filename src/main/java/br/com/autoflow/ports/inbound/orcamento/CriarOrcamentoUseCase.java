package br.com.autoflow.ports.inbound.orcamento;

import br.com.autoflow.domain.model.Orcamento;

import java.util.UUID;

public interface CriarOrcamentoUseCase {
    Orcamento criar(UUID idOs, Orcamento orcamento);
}
