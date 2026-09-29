package br.com.autoflow.ports.inbound.ordemservico;

import br.com.autoflow.domain.model.OrdemServico;

import java.util.UUID;

public interface BuscarOrdemServicoPorIdUseCase {
    OrdemServico buscarPorId(UUID id);
}
