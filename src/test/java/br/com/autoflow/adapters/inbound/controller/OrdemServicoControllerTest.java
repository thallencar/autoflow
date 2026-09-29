package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOSRequest;
import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusPagamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.HistoricoVeiculoResponse;
import br.com.autoflow.adapters.inbound.controller.dto.MetricaOsResponse;
import br.com.autoflow.adapters.inbound.controller.dto.OrdemServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrdemServicoResponse;
import br.com.autoflow.adapters.inbound.mapper.OrdemServicoMapper;
import br.com.autoflow.application.usecase.OrdemServicoUseCaseImpl;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.OrdemServico;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    @Mock
    private OrdemServicoUseCaseImpl service;

    @Mock
    private OrdemServicoMapper mapper; // Adicionado o mock do mapper que faltava

    @InjectMocks
    private OrdemServicoController controller;

    @Test
    void deveListarTodas() {
        Pageable pageable = PageRequest.of(0, 2);
        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(UUID.randomUUID());
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        Page<OrdemServico> pageDomain = new PageImpl<>(List.of(dominio), pageable, 1);

        when(service.listarTodas(pageable)).thenReturn(pageDomain);

        Page<OrdemServicoResponse> result = controller.listarTodas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(service).listarTodas(pageable);
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(id);
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        when(service.buscarPorId(id)).thenReturn(dominio);

        OrdemServicoResponse result = controller.buscarPorId(id);

        assertNotNull(result);
        assertEquals(id, result.idOs());
        verify(service).buscarPorId(id);
    }

    @Test
    void deveCriar() {
        OrdemServicoRequest request = new OrdemServicoRequest("relato", "diag", true, LocalDateTime.now(), LocalDateTime.now(), 1000, StatusOS.AGUARDANDO_APROVACAO, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(UUID.randomUUID());
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        when(service.criar(any(OrdemServico.class), eq(true))).thenReturn(dominio);

        OrdemServicoResponse result = controller.criar(request, true);

        assertNotNull(result);
        verify(service).criar(any(OrdemServico.class), eq(true));
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        OrdemServicoRequest request = new OrdemServicoRequest("relato", "diag", true, LocalDateTime.now(), LocalDateTime.now(), 1000, StatusOS.AGUARDANDO_APROVACAO, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(id);
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        when(service.atualizar(eq(id), any(OrdemServico.class))).thenReturn(dominio);

        OrdemServicoResponse result = controller.atualizar(id, request);

        assertNotNull(result);
        verify(service).atualizar(eq(id), any(OrdemServico.class));
    }

    @Test
    void deveAtualizarStatus() {
        UUID id = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.EM_DIAGNOSTICO, "analise");

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(id);
        dominio.setStatusOS(StatusOS.EM_DIAGNOSTICO);

        when(service.atualizarStatus(eq(id), eq(request.status()), eq(request.observacao()))).thenReturn(dominio);

        OrdemServicoResponse result = controller.atualizarStatus(id, request);

        assertNotNull(result);
        verify(service).atualizarStatus(eq(id), eq(request.status()), eq(request.observacao()));
    }

    @Test
    void deveObterMetricasPorOS() {
        UUID idOs = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        // 1. O Use Case retorna o domínio
        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(idOs);
        dominio.setDataInicioExecucao(agora);
        dominio.setDataFimExecucao(agora);
        dominio.setStatusOS(StatusOS.ENTREGUE);

        // 2. O DTO esperado no final
        MetricaOsResponse response = new MetricaOsResponse(
                idOs,
                "ENTREGUE",
                agora,
                agora,
                15L
        );

        when(service.obterMetricasPorOS(idOs)).thenReturn(dominio);
        when(mapper.toMetricaResponse(dominio)).thenReturn(response); // Mock do mapper no controller

        MetricaOsResponse result = controller.obterMetricasPorOS(idOs);

        assertEquals(response, result);
        verify(service).obterMetricasPorOS(idOs);
    }

    @Test
    void deveListarMetricas() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 2);

        // 1. O Use Case retorna uma página de Domínio
        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(UUID.randomUUID());
        Page<OrdemServico> pageDomain = new PageImpl<>(List.of(dominio), pageable, 1);

        // 2. O DTO correspondente
        MetricaOsResponse response = new MetricaOsResponse(
                dominio.getIdOs(),
                "EM_EXECUCAO",
                inicio,
                fim,
                15L
        );

        when(service.buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable)).thenReturn(pageDomain);
        when(mapper.toMetricaResponse(dominio)).thenReturn(response);

        Page<MetricaOsResponse> result = controller.listarMetricas(inicio, fim, StatusOS.EM_EXECUCAO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(service).buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable);
    }

    @Test
    void deveListarHistoricoPorVeiculo() {
        UUID idVeiculo = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 2);

        // 1. Criamos um objeto de domínio OrdemServico (que é o que o service agora retorna)
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setIdOs(UUID.randomUUID());
        ordemServico.setStatusOS(StatusOS.ENTREGUE);
        ordemServico.setDsRelatoCliente("relato");
        // (preencha outros campos se necessário para o seu teste)

        Page<OrdemServico> pageDomain = new PageImpl<>(List.of(ordemServico), pageable, 1);

        // 2. Criamos o DTO esperado que o mapper vai retornar
        HistoricoVeiculoResponse responseDto = new HistoricoVeiculoResponse(
                ordemServico.getIdOs(), ordemServico.getStatusOS(), "relato", "diag", 1000,
                LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        // 3. O service agora retorna Page<OrdemServico>
        when(service.obterHistoricoPorVeiculo(idVeiculo, pageable)).thenReturn(pageDomain);

        // 4. Mockamos o mapper para traduzir o Domínio para o DTO de histórico
        when(mapper.toHistoricoResponse(ordemServico)).thenReturn(responseDto);

        // Executa o método do controller
        Page<HistoricoVeiculoResponse> result = controller.listarHistoricoPorVeiculo(idVeiculo, pageable);

        // Validações
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto, result.getContent().get(0));

        // Verifica se ambos foram chamados corretamente
        verify(service).obterHistoricoPorVeiculo(idVeiculo, pageable);
        verify(mapper).toHistoricoResponse(ordemServico);
    }

    @Test
    void deveAtualizarStatusPagamento() {
        UUID id = UUID.randomUUID();
        AtualizarStatusPagamentoRequest request = new AtualizarStatusPagamentoRequest(StatusPagamento.PAGO);

        controller.atualizarStatusPagamento(id, request);

        verify(service).atualizarStatusPagamento(id, request.stPagamento());
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