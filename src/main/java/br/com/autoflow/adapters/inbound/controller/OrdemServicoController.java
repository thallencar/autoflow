package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.adapters.inbound.mapper.OrdemServicoMapper;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.inbound.ordemservico.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;
    private final AtualizarOrdemServicoUseCase atualizarOrdemServicoUseCase;
    private final AtualizarStatusOrdemServicoUseCase atualizarStatusOrdemServicoUseCase;
    private final BuscarOrdemServicoPorIdUseCase buscarOrdemServicoPorIdUseCase;
    private final DeletarOrdemServicoUseCase deletarOrdemServicoUseCase;
    private final ListarOrdemServicoUseCase listarOrdemServicoUseCase;
    private final OrdemServicoMapper ordemServicoMapper;

    @GetMapping("/ativas")
    public Page<OrdemServicoResponse> listarOsAtivas(Pageable pageable) {
        Page<OrdemServico> dominioPage = listarOrdemServicoUseCase.listarOsAtivas(pageable);
        return dominioPage.map(ordemServicoMapper::toResponse);
    }

    @GetMapping("/todas")
    public Page<OrdemServicoResponse> listarTodas(Pageable pageable) {
        Page<OrdemServico> dominioPage = listarOrdemServicoUseCase.listarTodas(pageable);
        return dominioPage.map(ordemServicoMapper::toResponse);
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscarPorId(@PathVariable UUID id) {
        OrdemServico dominio = buscarOrdemServicoPorIdUseCase.buscarPorId(id);
        return ordemServicoMapper.toResponse(dominio);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServicoResponse criar(@RequestBody @Valid OrdemServicoRequest request,
                                      @RequestParam(defaultValue = "false") boolean agendamento) {
        OrdemServico ordemServicoDomain = ordemServicoMapper.toDomain(request);
        OrdemServico salvoDomain = criarOrdemServicoUseCase.criar(ordemServicoDomain, agendamento);
        return ordemServicoMapper.toResponse(salvoDomain);
    }

    @PutMapping("/{id}")
    public OrdemServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
        OrdemServico ordemServicoDomain = ordemServicoMapper.toDomain(request);
        OrdemServico atualizadoDomain = atualizarOrdemServicoUseCase.atualizar(id, ordemServicoDomain);
        return ordemServicoMapper.toResponse(atualizadoDomain);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrdemServicoResponse atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarStatusOSRequest request
    ) {
        OrdemServico atualizadoDomain = atualizarStatusOrdemServicoUseCase.atualizarStatus(
                id,
                request.status(),
                request.observacao()
        );
        return ordemServicoMapper.toResponse(atualizadoDomain);
    }

    @GetMapping("/{idOs}/metricas")
    @ResponseStatus(HttpStatus.OK)
    public MetricaOsResponse obterMetricasPorOS(@PathVariable UUID idOs) {
        OrdemServico dominio = listarOrdemServicoUseCase.obterMetricasPorOS(idOs);
        return ordemServicoMapper.toMetricaResponse(dominio);
    }

    @GetMapping("/metricas")
    @ResponseStatus(HttpStatus.OK)
    public Page<MetricaOsResponse> listarMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusOS status,
            Pageable pageable) {
        Page<OrdemServico> dominioPage = listarOrdemServicoUseCase.buscarMetricasComFiltro(dataInicio, dataFim, status, pageable);
        return dominioPage.map(ordemServicoMapper::toMetricaResponse);
    }

    @PatchMapping("/{id}/pagamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarStatusPagamento(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusPagamentoRequest request) {

        atualizarStatusOrdemServicoUseCase.atualizarStatusPagamento(id, request.stPagamento());
    }

    @GetMapping("/filtro-status")
    @ResponseStatus(HttpStatus.OK)
    public Page<OrdemServicoResponse> listarPorStatus(
            @RequestParam StatusOS status,
            Pageable pageable) {
        Page<OrdemServico> dominioPage = listarOrdemServicoUseCase.listarPorStatus(status, pageable);
        return dominioPage.map(ordemServicoMapper::toResponse);
    }

    @GetMapping("/veiculo/{idVeiculo}/historico")
    @ResponseStatus(HttpStatus.OK)
    public Page<HistoricoVeiculoResponse> listarHistoricoPorVeiculo(@PathVariable UUID idVeiculo, Pageable pageable) {
        Page<OrdemServico> dominioPage = listarOrdemServicoUseCase.obterHistoricoPorVeiculo(idVeiculo, pageable);
        return dominioPage.map(ordemServicoMapper::toHistoricoResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarOrdemServicoUseCase.deletar(id);
    }

    @PostMapping("/processar-cancelamentos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forcarCancelamentoAutomatico() {
        atualizarStatusOrdemServicoUseCase.processarCancelamentosAutomaticos();
    }
}