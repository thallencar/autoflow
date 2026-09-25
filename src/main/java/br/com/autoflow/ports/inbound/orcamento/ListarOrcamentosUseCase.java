package br.com.autoflow.ports.inbound.orcamento;

import br.com.autoflow.domain.model.Orcamento;

import java.util.List;
import java.util.UUID;

public interface ListarOrcamentosUseCase {
    List<Orcamento> listarTodos();
    List<Orcamento> listarPorOrdemServico(UUID idOs);
}
