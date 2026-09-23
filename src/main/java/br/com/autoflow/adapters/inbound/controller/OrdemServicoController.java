package br.com.autoflow.adapter.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.application.usecase.OrdemServicoUseCase;
import br.com.autoflow.domain.enums.StatusOS;
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

    private final OrdemServicoUseCase ordemServicoUseCase;

    @GetMapping
    public Page<OrdemServicoResponse> listarTodas(Pageable pageable) {
        return ordemServicoUseCase.listarTodas(pageable);
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscarPorId(@PathVariable UUID id) {
        return ordemServicoUseCase.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServicoResponse criar(@RequestBody @Valid OrdemServicoRequest request,
                                      @RequestParam(defaultValue = "false") boolean agendamento) {
        return ordemServicoUseCase.criar(request, agendamento);
    }

    @PutMapping("/{id}")
    public OrdemServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
        return ordemServicoUseCase.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrdemServicoResponse atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarStatusOSRequest request
    ) {
        return ordemServicoUseCase.atualizarStatus(id, request);
    }

    @GetMapping("/{idOs}/metricas")
    @ResponseStatus(HttpStatus.OK)
    public MetricaOsResponse obterMetricasPorOS(@PathVariable UUID idOs) {
        return ordemServicoUseCase.obterMetricasPorOS(idOs);
    }

    @GetMapping("/metricas")
    @ResponseStatus(HttpStatus.OK)
    public Page<MetricaOsResponse> listarMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusOS status,
            Pageable pageable) {
        return ordemServicoUseCase.buscarMetricasComFiltro(dataInicio, dataFim, status, pageable);
    }

    @PatchMapping("/{id}/pagamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarStatusPagamento(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusPagamentoRequest request) {

        ordemServicoUseCase.atualizarStatusPagamento(id, request.stPagamento());
    }

    @GetMapping("/filtro-status")
    @ResponseStatus(HttpStatus.OK)
    public Page<OrdemServicoResponse> listarPorStatus(
            @RequestParam StatusOS status,
            Pageable pageable) {
        return ordemServicoUseCase.listarPorStatus(status, pageable);
    }

    @GetMapping("/veiculo/{idVeiculo}/historico")
    @ResponseStatus(HttpStatus.OK)
    public Page<HistoricoVeiculoResponse> listarHistoricoPorVeiculo(@PathVariable UUID idVeiculo, Pageable pageable) {
        return ordemServicoUseCase.obterHistoricoPorVeiculo(idVeiculo, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        ordemServicoUseCase.deletar(id);
    }

    @PostMapping("/processar-cancelamentos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forcarCancelamentoAutomatico() {
        ordemServicoUseCase.processarCancelamentosAutomaticos();
    }
}