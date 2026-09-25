package br.com.autoflow.ports.inbound.estoque;

import br.com.autoflow.domain.model.Estoque;

import java.math.BigDecimal;
import java.util.UUID;

public interface AtualizarEstoqueUseCase {
    Estoque adicionarQuantidade(UUID id, Integer quantidade);
    Estoque atualizarValorUnitario(UUID id, BigDecimal valorUnitario);
    Estoque atualizar(UUID id, Estoque estoque);
}
