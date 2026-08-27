package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.application.service.OrdemServicoService;
import br.com.autoflow.domain.enums.StatusOS;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService service;

    @GetMapping
    public Page<OrdemServicoResponse> listarTodas(Pageable pageable) {
        return service.listarTodas(pageable);
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServicoResponse criar(@RequestBody @Valid OrdemServicoRequest request,
                                      @RequestParam(defaultValue = "false") boolean agendamento ) {
        return service.criar(request, agendamento);
    }

    @PutMapping("/{id}")
    public OrdemServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
        return service.atualizar(id, request);
    }
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrdemServicoResponse atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarStatusOSRequest request
    ) {
        return service.atualizarStatus(id, request);
    }

    @GetMapping("/{idOs}/metricas")
    @ResponseStatus(HttpStatus.OK)
    public MetricaOsResponse obterMetricasPorOS(@PathVariable UUID idOs) {
        return service.obterMetricasPorOS(idOs);
    }

    @GetMapping("/metricas")
    @ResponseStatus(HttpStatus.OK)
    public Page<MetricaOsResponse> listarMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusOS status,
            Pageable pageable) {
        return service.buscarMetricasComFiltro(dataInicio, dataFim, status, pageable);
    }

    @PatchMapping("/{id}/pagamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarStatusPagamento(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusPagamentoRequest request) {

        service.atualizarStatusPagamento(id, request.stPagamento());
    }

    @GetMapping("/filtro-status")
    @ResponseStatus(HttpStatus.OK)
    public Page<OrdemServicoResponse> listarPorStatus(
            @RequestParam StatusOS status,
            Pageable pageable) {
        return service.listarPorStatus(status, pageable);
    }

    @GetMapping("/veiculo/{idVeiculo}/historico")
    @ResponseStatus(HttpStatus.OK)
    public Page<HistoricoVeiculoResponse> listarHistoricoPorVeiculo(@PathVariable UUID idVeiculo, Pageable pageable) {
        return service.obterHistoricoPorVeiculo(idVeiculo, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletar(id);
    }

    @PostMapping("/processar-cancelamentos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forcarCancelamentoAutomatico() {
        service.processarCancelamentosAutomaticos();
    }

}