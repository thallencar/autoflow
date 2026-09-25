package br.com.autoflow.ports.inbound.estoque;

import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoItemRequest;

import java.util.List;

public interface GerenciarReservaEstoqueUseCase {
    void reservarEstoqueParaItens(List<OrcamentoItemRequest> itensRequest);
    void devolverEstoqueDeItens(List<OrcamentoItemRequest> itensRequest);
}
