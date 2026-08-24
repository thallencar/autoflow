package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.infrastructure.mapper.OrdemServicoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private OrdemServicoMapper mapper;

    @Mock
    private OrdemServicoValidator validator;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private OrcamentoService orcamentoService;

    @InjectMocks
    private OrdemServicoService service;

    @Test
    @DisplayName("Deve listar todas as ordens de serviço (versão antiga List)")
    void deveListarTodasLista() {
        OrdemServico os = new OrdemServico();
        List<OrdemServico> lista = List.of(os);
        List<OrdemServicoResponse> listaResponse = List.of(mock(OrdemServicoResponse.class));

        when(repository.findAll()).thenReturn(lista);
        when(mapper.toResponseList(lista)).thenReturn(listaResponse);

        List<OrdemServicoResponse> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).findAll();
        verify(mapper).toResponseList(lista);
    }

    @Test
    @DisplayName("Deve buscar por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico();
        OrdemServicoResponse responseEsperado = mock(OrdemServicoResponse.class);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(mapper.toResponse(os)).thenReturn(responseEsperado);

        OrdemServicoResponse resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(responseEsperado, resultado);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por ID inexistente")
    void deveLancarExcecaoBuscarPorIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve criar Ordem de Serviço com sucesso")
    void deveCriarOrdemServico() {
        OrdemServicoRequest request = mock(OrdemServicoRequest.class);
        OrdemServico os = new OrdemServico();
        OrdemServicoResponse responseEsperado = mock(OrdemServicoResponse.class);

        when(repository.countByStatusOSNot(StatusOS.ENTREGUE)).thenReturn(1L);
        doNothing().when(validator).validarCriacao(request, true, 1L);
        when(mapper.toEntity(request)).thenReturn(os);
        when(repository.save(os)).thenReturn(os);
        when(mapper.toResponse(os)).thenReturn(responseEsperado);

        OrdemServicoResponse resultado = service.criar(request, true);

        assertNotNull(resultado);
        verify(validator).validarCriacao(request, true, 1L);
    }

    @Test
    @DisplayName("Deve atualizar Ordem de Serviço com sucesso")
    void deveAtualizarOrdemServico() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        List<UUID> idsOrcamentoList = List.of(UUID.randomUUID());

        OrdemServicoRequest request = mock(OrdemServicoRequest.class);

        when(request.idCliente()).thenReturn(clienteId);
        when(request.idsOrcamento()).thenReturn(idsOrcamentoList);

        OrdemServico os = new OrdemServico();
        OrdemServicoResponse responseEsperado = mock(OrdemServicoResponse.class);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        doNothing().when(validator).validarCliente(clienteId);
        doNothing().when(validator).validarOrcamentosParaOS(idsOrcamentoList);
        doNothing().when(mapper).updateEntityFromRequest(os, request);
        when(repository.save(os)).thenReturn(os);
        when(mapper.toResponse(os)).thenReturn(responseEsperado);

        OrdemServicoResponse resultado = service.atualizar(id, request);

        assertNotNull(resultado);
        verify(validator).validarCliente(clienteId);
        verify(validator).validarOrcamentosParaOS(idsOrcamentoList);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar OS inexistente")
    void deveLancarExcecaoAtualizarOsInexistente() {
        UUID id = UUID.randomUUID();
        OrdemServicoRequest request = mock(OrdemServicoRequest.class);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(id, request));
    }

    @Test
    @DisplayName("Deve atualizar status de pagamento com sucesso")
    void deveAtualizarStatusPagamento() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico();

        when(repository.findById(id)).thenReturn(Optional.of(os));
        doNothing().when(validator).validarAtualizacaoPagamento(os, StatusPagamento.PAGO);
        when(repository.save(os)).thenReturn(os);

        assertDoesNotThrow(() -> service.atualizarStatusPagamento(id, StatusPagamento.PAGO));
        verify(validator).validarAtualizacaoPagamento(os, StatusPagamento.PAGO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar pagamento de OS inexistente")
    void deveLancarExcecaoAtualizarPagamentoInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizarStatusPagamento(id, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve deletar OS com sucesso")
    void deveDeletarOs() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        assertDoesNotThrow(() -> service.deletar(id));
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar OS inexistente")
    void deveLancarExcecaoDeletarInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(id));
    }

    @Test
    @DisplayName("Deve atualizar status para EM_EXECUCAO deduzindo itens de orçamentos pendentes")
    void deveAtualizarStatusParaEmExecucaoComOrcamentoPendente() {
        UUID idOs = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.EM_EXECUCAO, "Executando");

        Orcamento orcamentoPendente = new Orcamento();
        orcamentoPendente.setStatus(StatusOrcamento.PENDENTE);

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);
        os.setIdsOrcamento(List.of(orcamentoPendente));

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);
        when(mapper.toResponse(os)).thenReturn(mock(OrdemServicoResponse.class));

        service.atualizarStatus(idOs, request);

        verify(orcamentoService).deduzirItensDoEstoque(orcamentoPendente);
        verify(repository).save(os);
    }

    @Test
    @DisplayName("Deve atualizar status para EM_DIAGNOSTICO alocando mecânico")
    void deveAtualizarStatusParaEmDiagnosticoComMecanico() {
        UUID idOs = UUID.randomUUID();
        UUID idMec = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.EM_DIAGNOSTICO, "Diagnóstico feito");

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.RECEBIDA);
        os.setIdFuncionario(idMec);

        Funcionario mecanico = mock(Funcionario.class); // <--- Mudado para mock

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        doNothing().when(validator).validarAlocacaoMecanico(idMec, idMec);
        doNothing().when(validator).validarDiagnosticoPreenchido(request.observacao());
        when(funcionarioRepository.findById(idMec)).thenReturn(Optional.of(mecanico));
        when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);
        when(mapper.toResponse(os)).thenReturn(mock(OrdemServicoResponse.class));

        service.atualizarStatus(idOs, request);

        verify(mecanico).ocupar(); // Agora o Mockito aceita o verify aqui
        verify(funcionarioRepository).save(mecanico);
    }

    @Test
    @DisplayName("Deve lançar exceção se mecânico não for encontrado no diagnóstico")
    void deveLancarExcecaoMecanicoNaoEncontradoNoDiagnostico() {
        UUID idOs = UUID.randomUUID();
        UUID idMec = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.EM_DIAGNOSTICO, "Diagnóstico");

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setIdFuncionario(idMec);

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        when(funcionarioRepository.findById(idMec)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizarStatus(idOs, request));
    }

    @Test
    @DisplayName("Deve atualizar status para FINALIZADA liberando o mecânico alocado")
    void deveAtualizarStatusParaFinalizadaLiberandoMecanico() {
        UUID idOs = UUID.randomUUID();
        UUID idMec = UUID.randomUUID();
        AtualizarStatusOSRequest request = new AtualizarStatusOSRequest(StatusOS.FINALIZADA, "Concluído");

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.EM_EXECUCAO);
        os.setIdFuncionario(idMec);

        Funcionario mecanico = mock(Funcionario.class);

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        when(funcionarioRepository.findById(idMec)).thenReturn(Optional.of(mecanico));
        when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);
        when(mapper.toResponse(os)).thenReturn(mock(OrdemServicoResponse.class));

        service.atualizarStatus(idOs, request);

        verify(mecanico).liberar();
        verify(funcionarioRepository).save(mecanico);
    }

    @Test
    @DisplayName("Deve obter métricas por ID da OS")
    void deveObterMetricasPorOs() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico();
        MetricaOsResponse metricas = mock(MetricaOsResponse.class);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(mapper.toMetricaResponse(os)).thenReturn(metricas);

        MetricaOsResponse resultado = service.obterMetricasPorOS(id);

        assertNotNull(resultado);
        assertEquals(metricas, resultado);
    }

    @Test
    @DisplayName("Deve buscar métricas com filtro paginadas")
    void deveBuscarMetricasComFiltro() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now();
        Pageable pageable = Pageable.unpaged();

        OrdemServico os = new OrdemServico();
        Page<OrdemServico> pageOs = new PageImpl<>(List.of(os));
        MetricaOsResponse metrica = mock(MetricaOsResponse.class);

        when(repository.findMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable)).thenReturn(pageOs);
        when(mapper.toMetricaResponse(os)).thenReturn(metrica);

        Page<MetricaOsResponse> resultado = service.buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve obter histórico por veículo com sucesso")
    void deveObterHistoricoPorVeiculo() {
        UUID idVeiculo = UUID.randomUUID();
        OrdemServico os = new OrdemServico();
        List<OrdemServico> lista = List.of(os);
        HistoricoVeiculoResponse historico = mock(HistoricoVeiculoResponse.class);

        doNothing().when(validator).validarVeiculoExiste(idVeiculo);
        when(repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo)).thenReturn(lista);
        when(mapper.toHistoricoResponse(os)).thenReturn(historico);

        List<HistoricoVeiculoResponse> resultado = service.obterHistoricoPorVeiculo(idVeiculo);

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção se histórico por veículo estiver vazio")
    void deveLancarExcecaoHistoricoVazio() {
        UUID idVeiculo = UUID.randomUUID();
        doNothing().when(validator).validarVeiculoExiste(idVeiculo);
        when(repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo)).thenReturn(Collections.emptyList());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.obterHistoricoPorVeiculo(idVeiculo));
    }

    @Test
    @DisplayName("Deve processar cancelamentos automáticos via agendamento")
    void deveProcessarCancelamentosAutomaticos() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStatusOS()).thenReturn(StatusOS.AGUARDANDO_APROVACAO);

        List<OrdemServico> lista = List.of(os);

        when(repository.findByStatusOS(StatusOS.AGUARDANDO_APROVACAO)).thenReturn(lista);

        doAnswer(invocation -> {
            when(os.getStatusOS()).thenReturn(StatusOS.CANCELADA);
            return null;
        }).when(os).verificarCancelamentoAutomatico(anyInt(), any(BigDecimal.class));

        when(repository.save(os)).thenReturn(os);

        assertDoesNotThrow(() -> service.processarCancelamentosAutomaticos());
        verify(repository).save(os);
    }

    @Test
    @DisplayName("Deve processar abandono técnico via agendamento")
    void deveProcessarAbandonoTecnico() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStatusOS()).thenReturn(StatusOS.AGUARDANDO_APROVACAO);

        List<OrdemServico> lista = List.of(os);

        when(repository.findByStatusOS(StatusOS.AGUARDANDO_APROVACAO)).thenReturn(lista);

        doAnswer(invocation -> {
            when(os.getStatusOS()).thenReturn(StatusOS.CANCELADA); // ou o status correspondente a abandono
            return null;
        }).when(os).verificarAbandonoTecnico(anyInt());

        when(repository.save(os)).thenReturn(os);

        assertDoesNotThrow(() -> service.processarAbandonoTecnico());
        verify(repository).save(os);
    }
}