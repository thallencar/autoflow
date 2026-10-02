package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOSRequest;
import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusPagamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.HistoricoVeiculoResponse;
import br.com.autoflow.adapters.inbound.controller.dto.MetricaOsResponse;
import br.com.autoflow.adapters.inbound.controller.dto.OrdemServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrdemServicoResponse;
import br.com.autoflow.adapters.inbound.mapper.OrdemServicoMapper;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.inbound.ordemservico.*;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    @Mock
    private CriarOrdemServicoUseCase criarOrdemServicoUseCase;

    @Mock
    private AtualizarOrdemServicoUseCase atualizarOrdemServicoUseCase;

    @Mock
    private AtualizarStatusOrdemServicoUseCase atualizarStatusOrdemServicoUseCase;

    @Mock
    private BuscarOrdemServicoPorIdUseCase buscarOrdemServicoPorIdUseCase;

    @Mock
    private DeletarOrdemServicoUseCase deletarOrdemServicoUseCase;

    @Mock
    private ListarOrdemServicoUseCase listarOrdemServicoUseCase;

    @Mock
    private OrdemServicoMapper ordemServicoMapper;

    @InjectMocks
    private OrdemServicoController controller;

    private OrdemServicoResponse criarResponseMock(UUID idOs) {
        LocalDateTime agora = LocalDateTime.now();
        return new OrdemServicoResponse(
                idOs,
                StatusOS.AGUARDANDO_APROVACAO,
                "relato",
                "diag",
                true,
                agora,
                1000,
                agora,
                agora,
                agora,
                agora,
                agora,
                agora,
                agora,
                agora,
                "PENDENTE",
                "motivo",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of()
        );
    }

    @Test
    void deveListarTodas() {
        Pageable pageable = PageRequest.of(0, 2);
        OrdemServico dominio = new OrdemServico();
        UUID idOs = UUID.randomUUID();
        dominio.setIdOs(idOs);
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        OrdemServicoResponse responseDto = criarResponseMock(idOs);
        Page<OrdemServico> pageDomain = new PageImpl<>(List.of(dominio), pageable, 1);

        when(listarOrdemServicoUseCase.listarOsAtivas(pageable)).thenReturn(pageDomain);
        when(ordemServicoMapper.toResponse(dominio)).thenReturn(responseDto);

        Page<OrdemServicoResponse> result = controller.listarOsAtivas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(listarOrdemServicoUseCase).listarOsAtivas(pageable);
        verify(ordemServicoMapper).toResponse(dominio);
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(id);
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        OrdemServicoResponse responseDto = criarResponseMock(id);

        when(buscarOrdemServicoPorIdUseCase.buscarPorId(id)).thenReturn(dominio);
        when(ordemServicoMapper.toResponse(dominio)).thenReturn(responseDto);

        OrdemServicoResponse result = controller.buscarPorId(id);

        assertNotNull(result);
        assertEquals(id, result.idOs());
        verify(buscarOrdemServicoPorIdUseCase).buscarPorId(id);
        verify(ordemServicoMapper).toResponse(dominio);
    }

    @Test
    void deveCriar() {
        OrdemServicoRequest request = new OrdemServicoRequest("relato", "diag", true, LocalDateTime.now(), LocalDateTime.now(), 1000, StatusOS.AGUARDANDO_APROVACAO, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());

        OrdemServico dominio = new OrdemServico();
        UUID idOs = UUID.randomUUID();
        dominio.setIdOs(idOs);
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        OrdemServicoResponse responseDto = criarResponseMock(idOs);

        when(ordemServicoMapper.toDomain(request)).thenReturn(dominio);
        when(criarOrdemServicoUseCase.criar(any(OrdemServico.class), eq(true))).thenReturn(dominio);
        when(ordemServicoMapper.toResponse(dominio)).thenReturn(responseDto);

        OrdemServicoResponse result = controller.criar(request, true);

        assertNotNull(result);
        verify(criarOrdemServicoUseCase).criar(any(OrdemServico.class), eq(true));
        verify(ordemServicoMapper).toResponse(dominio);
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        OrdemServicoRequest request = new OrdemServicoRequest("relato", "diag", true, LocalDateTime.now(), LocalDateTime.now(), 1000, StatusOS.AGUARDANDO_APROVACAO, "PENDENTE", "motivo", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of());

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(id);
        dominio.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        OrdemServicoResponse responseDto = criarResponseMock(id);

        when(ordemServicoMapper.toDomain(request)).thenReturn(dominio);
        when(atualizarOrdemServicoUseCase.atualizar(eq(id), any(OrdemServico.class))).thenReturn(dominio);
        when(ordemServicoMapper.toResponse(dominio)).thenReturn(responseDto);

        OrdemServicoResponse result = controller.atualizar(id, request);

        assertNotNull(result);
        verify(atualizarOrdemServicoUseCase).atualizar(eq(id), any(OrdemServico.class));
        verify(ordemServicoMapper).toResponse(dominio);
    }

    @Test
    void deveAtualizarStatus() {
        UUID id = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.EM_DIAGNOSTICO, "analise");

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(id);
        dominio.setStatusOS(StatusOS.EM_DIAGNOSTICO);

        OrdemServicoResponse responseDto = criarResponseMock(id);

        // Usamos any() para evitar falhas de matching estrito de parâmetros no Mockito
        when(atualizarStatusOrdemServicoUseCase.atualizarStatus(eq(id), any(StatusOS.class), any(String.class)))
                .thenReturn(dominio);

        when(ordemServicoMapper.toResponse(any(OrdemServico.class)))
                .thenReturn(responseDto);

        OrdemServicoResponse result = controller.atualizarStatus(id, request);

        assertNotNull(result, "O resultado não deveria ser nulo!");
        assertEquals(id, result.idOs());

        verify(atualizarStatusOrdemServicoUseCase).atualizarStatus(eq(id), eq(request.status()), eq(request.observacao()));
        verify(ordemServicoMapper).toResponse(any(OrdemServico.class));
    }

    @Test
    void deveObterMetricasPorOS() {
        UUID idOs = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(idOs);
        dominio.setDataInicioExecucao(agora);
        dominio.setDataFimExecucao(agora);
        dominio.setStatusOS(StatusOS.ENTREGUE);

        MetricaOsResponse response = new MetricaOsResponse(
                idOs,
                "ENTREGUE",
                agora,
                agora,
                15L
        );

        when(listarOrdemServicoUseCase.obterMetricasPorOS(idOs)).thenReturn(dominio);
        when(ordemServicoMapper.toMetricaResponse(dominio)).thenReturn(response);

        MetricaOsResponse result = controller.obterMetricasPorOS(idOs);

        assertEquals(response, result);
        verify(listarOrdemServicoUseCase).obterMetricasPorOS(idOs);
        verify(ordemServicoMapper).toMetricaResponse(dominio);
    }

    @Test
    void deveListarMetricas() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 2);

        OrdemServico dominio = new OrdemServico();
        dominio.setIdOs(UUID.randomUUID());
        Page<OrdemServico> pageDomain = new PageImpl<>(List.of(dominio), pageable, 1);

        MetricaOsResponse response = new MetricaOsResponse(
                dominio.getIdOs(),
                "EM_EXECUCAO",
                inicio,
                fim,
                15L
        );

        when(listarOrdemServicoUseCase.buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable)).thenReturn(pageDomain);
        when(ordemServicoMapper.toMetricaResponse(dominio)).thenReturn(response);

        Page<MetricaOsResponse> result = controller.listarMetricas(inicio, fim, StatusOS.EM_EXECUCAO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(listarOrdemServicoUseCase).buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable);
        verify(ordemServicoMapper).toMetricaResponse(dominio);
    }

    @Test
    void deveListarHistoricoPorVeiculo() {
        UUID idVeiculo = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 2);

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setIdOs(UUID.randomUUID());
        ordemServico.setStatusOS(StatusOS.ENTREGUE);
        ordemServico.setDsRelatoCliente("relato");

        Page<OrdemServico> pageDomain = new PageImpl<>(List.of(ordemServico), pageable, 1);

        HistoricoVeiculoResponse responseDto = new HistoricoVeiculoResponse(
                ordemServico.getIdOs(), ordemServico.getStatusOS(), "relato", "diag", 1000,
                LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        when(listarOrdemServicoUseCase.obterHistoricoPorVeiculo(idVeiculo, pageable)).thenReturn(pageDomain);
        when(ordemServicoMapper.toHistoricoResponse(ordemServico)).thenReturn(responseDto);

        Page<HistoricoVeiculoResponse> result = controller.listarHistoricoPorVeiculo(idVeiculo, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto, result.getContent().get(0));

        verify(listarOrdemServicoUseCase).obterHistoricoPorVeiculo(idVeiculo, pageable);
        verify(ordemServicoMapper).toHistoricoResponse(ordemServico);
    }

    @Test
    void deveAtualizarStatusPagamento() {
        UUID id = UUID.randomUUID();
        AtualizarStatusPagamentoRequest request = new AtualizarStatusPagamentoRequest(StatusPagamento.PAGO);

        controller.atualizarStatusPagamento(id, request);

        verify(atualizarStatusOrdemServicoUseCase).atualizarStatusPagamento(id, request.stPagamento());
    }

    @Test
    void deveDeletar() {
        UUID id = UUID.randomUUID();

        controller.deletar(id);

        verify(deletarOrdemServicoUseCase).deletar(id);
    }

    @Test
    void deveForcarCancelamentoAutomatico() {
        controller.forcarCancelamentoAutomatico();

        verify(atualizarStatusOrdemServicoUseCase).processarCancelamentosAutomaticos();
    }
}