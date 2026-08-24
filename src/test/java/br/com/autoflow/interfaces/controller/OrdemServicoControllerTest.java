package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.AtualizarStatusOSRequest;
import br.com.autoflow.application.dto.AtualizarStatusPagamentoRequest;
import br.com.autoflow.application.dto.HistoricoVeiculoResponse;
import br.com.autoflow.application.dto.MetricaOsResponse;
import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.application.service.OrdemServicoService;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusPagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    @Mock
    private OrdemServicoService service;

    @InjectMocks
    private OrdemServicoController controller;

    @Test
    void deveListarTodas() {
        OrdemServicoResponse response = new OrdemServicoResponse(UUID.randomUUID(), StatusOS.AGUARDANDO_APROVACAO, "relato", "diag", true, LocalDateTime.now(), 1000, LocalDateTime.now(), null, null, null, null, null, null, null, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());
        when(service.listarTodas()).thenReturn(List.of(response));

        List<OrdemServicoResponse> result = controller.listarTodas();

        assertEquals(List.of(response), result);
        verify(service).listarTodas();
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        OrdemServicoResponse response = new OrdemServicoResponse(id, StatusOS.AGUARDANDO_APROVACAO, "relato", "diag", true, LocalDateTime.now(), 1000, LocalDateTime.now(), null, null, null, null, null, null, null, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());
        when(service.buscarPorId(id)).thenReturn(response);

        OrdemServicoResponse result = controller.buscarPorId(id);

        assertEquals(response, result);
        verify(service).buscarPorId(id);
    }

    @Test
    void deveCriar() {
        OrdemServicoRequest request = new OrdemServicoRequest("relato", "diag", true, LocalDateTime.now(), LocalDateTime.now(), 1000, StatusOS.AGUARDANDO_APROVACAO, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());
        OrdemServicoResponse response = new OrdemServicoResponse(UUID.randomUUID(), StatusOS.AGUARDANDO_APROVACAO, "relato", "diag", true, LocalDateTime.now(), 1000, LocalDateTime.now(), null, null, null, null, null, null, null, "PENDENTE", "motivo", request.idCliente(), request.idVeiculo(), request.idFuncionario(), request.idsOrcamento());
        when(service.criar(request, true)).thenReturn(response);

        OrdemServicoResponse result = controller.criar(request, true);

        assertEquals(response, result);
        verify(service).criar(request, true);
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        OrdemServicoRequest request = new OrdemServicoRequest("relato", "diag", true, LocalDateTime.now(), LocalDateTime.now(), 1000, StatusOS.AGUARDANDO_APROVACAO, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());
        OrdemServicoResponse response = new OrdemServicoResponse(id, StatusOS.AGUARDANDO_APROVACAO, "relato", "diag", true, LocalDateTime.now(), 1000, LocalDateTime.now(), null, null, null, null, null, null, null, "PENDENTE", "motivo", request.idCliente(), request.idVeiculo(), request.idFuncionario(), request.idsOrcamento());
        when(service.atualizar(id, request)).thenReturn(response);

        OrdemServicoResponse result = controller.atualizar(id, request);

        assertEquals(response, result);
        verify(service).atualizar(id, request);
    }

    @Test
    void deveAtualizarStatus() {
        UUID id = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.EM_DIAGNOSTICO, "analise");
        OrdemServicoResponse response = new OrdemServicoResponse(id, StatusOS.EM_DIAGNOSTICO, "relato", "diag", true, LocalDateTime.now(), 1000, LocalDateTime.now(), null, null, null, null, null, null, null, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());
        when(service.atualizarStatus(id, request)).thenReturn(response);

        OrdemServicoResponse result = controller.atualizarStatus(id, request);

        assertEquals(response, result);
        verify(service).atualizarStatus(id, request);
    }

    @Test
    void deveObterMetricasPorOS() {
        UUID idOs = UUID.randomUUID();
        MetricaOsResponse response = new MetricaOsResponse(idOs, "EM_EXECUCAO", 60, 45L, 15L, LocalDateTime.now(), LocalDateTime.now());
        when(service.obterMetricasPorOS(idOs)).thenReturn(response);

        MetricaOsResponse result = controller.obterMetricasPorOS(idOs);

        assertEquals(response, result);
        verify(service).obterMetricasPorOS(idOs);
    }

    @Test
    void deveListarMetricas() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 2);
        MetricaOsResponse response = new MetricaOsResponse(UUID.randomUUID(), "EM_EXECUCAO", 60, 45L, 15L, inicio, fim);
        Page<MetricaOsResponse> page = new PageImpl<>(List.of(response), pageable, 1);
        when(service.buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable)).thenReturn(page);

        Page<MetricaOsResponse> result = controller.listarMetricas(inicio, fim, StatusOS.EM_EXECUCAO, pageable);

        assertEquals(page, result);
        verify(service).buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable);
    }

    @Test
    void deveAtualizarStatusPagamento() {
        UUID id = UUID.randomUUID();
        AtualizarStatusPagamentoRequest request = new AtualizarStatusPagamentoRequest(StatusPagamento.PAGO);

        controller.atualizarStatusPagamento(id, request);

        verify(service).atualizarStatusPagamento(id, request.stPagamento());
    }

    @Test
    void deveListarHistoricoPorVeiculo() {
        UUID idVeiculo = UUID.randomUUID();
        HistoricoVeiculoResponse response = new HistoricoVeiculoResponse(UUID.randomUUID(), StatusOS.ENTREGUE, "relato", "diag", 1000, LocalDateTime.now(), LocalDateTime.now(), List.of());
        when(service.obterHistoricoPorVeiculo(idVeiculo)).thenReturn(List.of(response));

        List<HistoricoVeiculoResponse> result = controller.listarHistoricoPorVeiculo(idVeiculo);

        assertEquals(List.of(response), result);
        verify(service).obterHistoricoPorVeiculo(idVeiculo);
    }

    @Test
    void deveDeletar() {
        UUID id = UUID.randomUUID();

        controller.deletar(id);

        verify(service).deletar(id);
    }

    @Test
    void deveForcarCancelamentoAutomatico() {
        controller.forcarCancelamentoAutomatico();

        verify(service).processarCancelamentosAutomaticos();
    }
}
