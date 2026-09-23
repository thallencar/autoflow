package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoResponse;
import br.com.autoflow.application.usecase.OrcamentoUseCase;
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

    private final OrcamentoUseCase orcamentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoResponse criar(@RequestBody @Valid OrcamentoRequest request) {
        return orcamentoService.criar(request);
    }

    @GetMapping
    public List<OrcamentoResponse> listarTodos() {
        return orcamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public OrcamentoResponse buscarPorId(@PathVariable UUID id) {
        return orcamentoService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        orcamentoService.delete(id);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrcamentoResponse atualizarStatus(@PathVariable UUID id,
                                             @Valid @RequestBody AtualizarStatusOrcamentoRequest request) {
        return orcamentoService.atualizarStatus(id, request);
    }

    @GetMapping("/ordem-servico/{idOs}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrcamentoResponse> listarPorOrcamentoOrdemDeServico(@PathVariable UUID idOs) {
        return orcamentoService.listarPorOrdemServico(idOs);
    }
}