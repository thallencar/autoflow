package br.com.autoflow.ports.inbound.ordemservico;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusPagamento; // Importe se necessário
import br.com.autoflow.domain.model.OrdemServico;

import java.util.UUID;

public interface AtualizarStatusOrdemServicoUseCase {

    OrdemServico atualizarStatus(UUID idOS, StatusOS novoStatus, String observacao);

    void atualizarStatusPagamento(UUID id, StatusPagamento novoStatus);

    void processarCancelamentosAutomaticos();
}