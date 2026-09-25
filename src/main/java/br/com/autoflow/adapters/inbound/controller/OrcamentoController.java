package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoResponse;
import br.com.autoflow.adapters.inbound.mapper.OrcamentoMapper;
import br.com.autoflow.application.usecase.OrcamentoUseCaseImpl;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.ports.inbound.orcamento.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private final CriarOrcamentoUseCase criarOrcamentoUseCase;
    private final AtualizarStatusOrcamentoUseCase atualizarStatusOrcamentoUseCase;
    private final ListarOrcamentosUseCase listarOrcamentosUseCase;
    private final BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase;
    private final DeletarOrcamentoUseCase deletarOrcamentoUseCase;
    private final OrcamentoUseCaseImpl orcamentoUseCaseAuxiliar;
    private final OrcamentoMapper orcamentoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoResponse criar(@RequestBody @Valid OrcamentoRequest request) {
        Orcamento domain = orcamentoMapper.toEntity(request);
        Orcamento salvo = criarOrcamentoUseCase.criar(request.idOs(), domain);
        return mapToResponseComAvisos(salvo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrcamentoResponse> listarTodos() {
        return listarOrcamentosUseCase.listarTodos().stream()
                .map(this::mapToResponseComAvisos)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrcamentoResponse buscarPorId(@PathVariable UUID id) {
        Orcamento orcamento = buscarOrcamentoPorIdUseCase.buscarPorId(id);
        return mapToResponseComAvisos(orcamento);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deletarOrcamentoUseCase.deletar(id);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrcamentoResponse atualizarStatus(@PathVariable UUID id,
                                             @Valid @RequestBody AtualizarStatusOrcamentoRequest request) {
        Orcamento atualizado = atualizarStatusOrcamentoUseCase.atualizarStatus(id, request.status());
        return mapToResponseComAvisos(atualizado);
    }

    @GetMapping("/ordem-servico/{idOs}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrcamentoResponse> listarPorOrcamentoOrdemDeServico(@PathVariable UUID idOs) {
        return listarOrcamentosUseCase.listarPorOrdemServico(idOs).stream()
                .map(this::mapToResponseComAvisos)
                .toList();
    }

    private OrcamentoResponse mapToResponseComAvisos(Orcamento orcamento) {
        OrcamentoResponse response = orcamentoMapper.toResponse(orcamento);
        List<String> avisos = orcamentoucVerificarAvisos(orcamento);

        return new OrcamentoResponse(
                response.id(),
                response.idOs(),
                response.tipoOrcamento(),
                response.status(),
                response.dataCriacao(),
                response.dataExpiracao(),
                response.dataDecisao(),
                response.subtotalPecas(),
                response.maoObra(),
                response.total(),
                response.servicos(),
                avisos
        );
    }

    private List<String> orcamentoucVerificarAvisos(Orcamento orcamento) {
        // Delega verificação de avisos de estoque para a camada de serviço/use case
        if (orcamentoUseCaseAuxiliar instanceof OrcamentoUseCaseImpl impl) {
            return impl.verificarAvisosEstoque(orcamento);
        }
        return List.of();
    }
}