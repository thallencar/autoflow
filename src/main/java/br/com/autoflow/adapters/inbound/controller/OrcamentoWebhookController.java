package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoResponse;
import br.com.autoflow.adapters.inbound.mapper.OrcamentoMapper;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.ports.inbound.orcamento.AtualizarStatusOrcamentoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("orcamentos/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhook de Orçamentos", description = "Endpoints para recebimento de notificações externas de aprovação ou recusa do cliente")
public class OrcamentoWebhookController {

    private final AtualizarStatusOrcamentoUseCase atualizarStatusOrcamentoUseCase;
    private final OrcamentoMapper orcamentoMapper;

    @PostMapping("/{id}/resposta")
    @Operation(summary = "Recebe notificação externa de aprovação ou recusa do orçamento pelo cliente")
    @ResponseStatus(HttpStatus.OK)
    public OrcamentoResponse receberRespostaCliente(
            @PathVariable UUID id,
            @RequestParam StatusOrcamento statusResposta) {
        Orcamento orcamento = atualizarStatusOrcamentoUseCase.atualizarStatus(id, statusResposta);
        return orcamentoMapper.toResponse(orcamento);
    }
}