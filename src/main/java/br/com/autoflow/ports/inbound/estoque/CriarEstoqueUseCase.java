package br.com.autoflow.ports.inbound.estoque;

import br.com.autoflow.domain.model.Estoque;

public interface CriarEstoqueUseCase {
    Estoque criar(Estoque estoque);
}
