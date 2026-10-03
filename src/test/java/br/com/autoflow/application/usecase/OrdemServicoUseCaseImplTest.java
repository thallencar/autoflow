package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.outbound.persistence.mapper.OrdemServicoEntityMapper;
import br.com.autoflow.application.validator.OrdemServicoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
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
class OrdemServicoUseCaseImplTest {

    @Mock
    private OrdemServicoRepositoryPort repository;

    @Mock
    private OrdemServicoEntityMapper entityMapper;

    @Mock
    private OrdemServicoValidator validator;

    @Mock
    private FuncionarioRepositoryPort funcionarioRepository;

    @InjectMocks
    private OrdemServicoUseCaseImpl service;

    @Test
    @DisplayName("Deve listar todas as ordens de serviço de forma paginada")
    void deveListarTodasPaginada() {
        Pageable pageable = Pageable.unpaged();
        OrdemServico os = new OrdemServico();
        Page<OrdemServico> pageOs = new PageImpl<>(List.of(os));

        when(repository.findAll(pageable)).thenReturn(pageOs);

        Page<OrdemServico> resultado = service.listarTodas(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());

        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico();

        when(repository.findById(id)).thenReturn(Optional.of(os));

        OrdemServico resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(os, resultado);
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
        OrdemServico os = new OrdemServico();

        List<StatusOS> statusIgnorados = List.of(StatusOS.ENTREGUE, StatusOS.CANCELADA);
        when(repository.countByStatusOSNotIn(statusIgnorados)).thenReturn(1L);
        doNothing().when(validator).validarCriacao(os, true, 1L);
        when(repository.save(os)).thenReturn(os);

        OrdemServico resultado = service.criar(os, true);

        assertNotNull(resultado);
        verify(validator).validarCriacao(os, true, 1L);
    }

    @Test
    @DisplayName("Deve atualizar Ordem de Serviço com sucesso")
    void deveAtualizarOrdemServico() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        OrdemServico osExistente = new OrdemServico();
        osExistente.setIdOs(id);

        OrdemServico osAtualizada = new OrdemServico();
        osAtualizada.setIdCliente(clienteId);
        osAtualizada.setIdsOrcamento(List.of());

        when(repository.findById(id)).thenReturn(Optional.of(osExistente));
        doNothing().when(validator).validarCliente(clienteId);
        doNothing().when(validator).validarOrcamentosParaOS(anyList());
        doNothing().when(validator).validarAlteracaoMecanico(any(), eq(id));
        when(repository.save(osExistente)).thenReturn(osExistente);

        OrdemServico resultado = service.atualizar(id, osAtualizada);

        assertNotNull(resultado);
        verify(validator).validarCliente(clienteId);
        verify(validator).validarOrcamentosParaOS(anyList());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar OS inexistente")
    void deveLancarExcecaoAtualizarOsInexistente() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(id, os));
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
        OrdemServico os = new OrdemServico();
        os.setIdOs(id);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        doNothing().when(repository).deleteById(id);

        assertDoesNotThrow(() -> service.deletar(id));
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar OS inexistente")
    void deveLancarExcecaoDeletarInexistente() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(id));
    }

    @Test
    @DisplayName("Deve atualizar status para EM_EXECUCAO deduzindo itens de orçamentos pendentes")
    void deveAtualizarStatusParaEmExecucaoComOrcamentoPendente() {
        UUID idOs = UUID.randomUUID();

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        Orcamento orcamentoPendente = new Orcamento(
                UUID.randomUUID(),
                null,
                StatusOrcamento.PENDENTE,
                null,
                null,
                null,
                null,
                null,
                null,
                os,
                null,
                null
        );

        os.setIdsOrcamento(List.of(orcamentoPendente));

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);

        service.atualizarStatus(idOs, StatusOS.EM_EXECUCAO, "Executando");

        verify(repository).save(os);
    }

    @Test
    @DisplayName("Deve atualizar status para EM_DIAGNOSTICO com sucesso")
    void deveAtualizarStatusParaEmDiagnosticoComMecanico() {
        UUID idOs = UUID.randomUUID();
        UUID idFuncionario = UUID.randomUUID();

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.RECEBIDA);
        os.setIdFuncionario(idFuncionario);

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);

        assertDoesNotThrow(() -> service.atualizarStatus(idOs, StatusOS.EM_DIAGNOSTICO, "Diagnóstico feito"));
        verify(repository).save(os);
    }

    @Test
    @DisplayName("Deve lançar exceção se status exigir requisitos não preenchidos (ex: diagnóstico vazio)")
    void deveLancarExcecaoRequisitosStatus() {
        UUID idOs = UUID.randomUUID();

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.EM_DIAGNOSTICO);

        when(repository.findById(idOs)).thenReturn(Optional.of(os));

        lenient().doThrow(new RegraNegocioException("Diagnóstico obrigatório"))
                .when(validator).validarDiagnosticoPreenchido(anyString());

        assertThrows(RegraNegocioException.class, () -> service.atualizarStatus(idOs, StatusOS.AGUARDANDO_APROVACAO, ""));
    }

    @Test
    @DisplayName("Deve atualizar status para FINALIZADA liberando o mecânico alocado")
    void deveAtualizarStatusParaFinalizadaLiberandoMecanico() {
        UUID idOs = UUID.randomUUID();
        UUID idMec = UUID.randomUUID();

        OrdemServico os = new OrdemServico();
        os.setIdOs(idOs);
        os.setStatusOS(StatusOS.EM_EXECUCAO);
        os.setIdFuncionario(idMec);

        Funcionario mecanico = mock(Funcionario.class);

        when(repository.findById(idOs)).thenReturn(Optional.of(os));
        when(funcionarioRepository.findById(idMec)).thenReturn(Optional.of(mecanico));
        when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        service.atualizarStatus(idOs, StatusOS.FINALIZADA, "Concluído");

        verify(mecanico).liberar();
        verify(funcionarioRepository).save(mecanico);
    }

    @Test
    @DisplayName("Deve obter métricas por ID da OS")
    void deveObterMetricasPorOs() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico();

        when(repository.findById(id)).thenReturn(Optional.of(os));

        OrdemServico resultado = service.obterMetricasPorOS(id);

        assertNotNull(resultado);
        assertEquals(os, resultado);
    }

    @Test
    @DisplayName("Deve buscar métricas com filtro paginadas")
    void deveBuscarMetricasComFiltro() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now();
        Pageable pageable = Pageable.unpaged();

        OrdemServico os = new OrdemServico();
        Page<OrdemServico> pageOs = new PageImpl<>(List.of(os));

        when(repository.findMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable)).thenReturn(pageOs);

        Page<OrdemServico> resultado = service.buscarMetricasComFiltro(inicio, fim, StatusOS.EM_EXECUCAO, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve obter histórico por veículo paginado com sucesso")
    void deveObterHistoricoPorVeiculoPaginado() {
        UUID idVeiculo = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        OrdemServico os = new OrdemServico();
        Page<OrdemServico> pageOs = new PageImpl<>(List.of(os));

        doNothing().when(validator).validarVeiculoExiste(idVeiculo);
        when(repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo, pageable)).thenReturn(pageOs);

        Page<OrdemServico> resultado = service.obterHistoricoPorVeiculo(idVeiculo, pageable);

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        verify(validator).validarVeiculoExiste(idVeiculo);
        verify(repository).findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo, pageable);
    }

    @Test
    @DisplayName("Deve lançar exceção se histórico por veículo paginado estiver vazio")
    void deveLancarExcecaoHistoricoVazioPaginado() {
        UUID idVeiculo = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        Page<OrdemServico> pageVazio = new PageImpl<>(Collections.emptyList());

        doNothing().when(validator).validarVeiculoExiste(idVeiculo);
        when(repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo, pageable)).thenReturn(pageVazio);

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.obterHistoricoPorVeiculo(idVeiculo, pageable));
    }

    @Test
    @DisplayName("Deve processar cancelamentos automáticos via agendamento")
    void deveProcessarCancelamentosAutomaticos() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStatusOS()).thenReturn(StatusOS.AGUARDANDO_APROVACAO);

        Page<OrdemServico> pagina = new PageImpl<>(List.of(os));

        when(repository.findByStatusOS(eq(StatusOS.AGUARDANDO_APROVACAO), any(Pageable.class))).thenReturn(pagina);

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

        Page<OrdemServico> pagina = new PageImpl<>(List.of(os));

        when(repository.findByStatusOS(eq(StatusOS.AGUARDANDO_APROVACAO), any(Pageable.class))).thenReturn(pagina);

        doAnswer(invocation -> {
            when(os.getStatusOS()).thenReturn(StatusOS.CANCELADA);
            return null;
        }).when(os).verificarAbandonoTecnico(anyInt());

        when(repository.save(os)).thenReturn(os);

        assertDoesNotThrow(() -> service.processarAbandonoTecnico());
        verify(repository).save(os);
    }
}