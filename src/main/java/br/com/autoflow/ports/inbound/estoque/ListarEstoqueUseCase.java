package br.com.autoflow.ports.inbound.estoque;

import br.com.autoflow.domain.model.Estoque;

import java.util.List;

public interface ListarEstoqueUseCase {
    List<Estoque> listarTodos();
    List<Estoque> listarInsumosComEstoqueBaixo();
}
