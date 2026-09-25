package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.OrcamentoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
import br.com.autoflow.domain.model.OrcamentoServico;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoUseCaseImplTest {

    @Mock
    private OrcamentoRepositoryPort orcamentoRepository;

    @Mock
    private OrdemServicoRepositoryPort ordemServicoRepository;

    @Mock
    private OrcamentoExpiradoUseCase orcamentoExpiradoUseCase;

    @Mock
    private EstoqueRepositoryPort estoqueRepository;

    @Mock
    private OrcamentoValidator orcamentoValidator;

    @InjectMocks
    private OrcamentoUseCaseImpl orcamentoService;

    private Orcamento criarOrcamentoMock() {
        return new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                null, LocalDateTime.now().plusDays(1), null, null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("Deve criar um orçamento inicial com sucesso")
    void deveCriarOrcamentoComSucesso() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();
        OrdemServico ordemServico = new OrdemServico();

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(ordemServico));
        doNothing().when(orcamentoValidator).validarCriacao(eq(idOs), any(Orcamento.class));
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        Orcamento resultado = orcamentoService.criar(idOs, orcamento);

        assertNotNull(resultado);
        verify(orcamentoValidator, times(1)).validarCriacao(eq(idOs), any(Orcamento.class));
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve criar um orçamento complementar com sucesso")
    void deveCriarOrcamentoComplementarComSucesso() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();
        orcamento.setTipoOrcamento(TipoOrcamento.COMPLEMENTAR);
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setStatusOS(StatusOS.EM_EXECUCAO);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(ordemServico));
        doNothing().when(orcamentoValidator).validarCriacao(eq(idOs), any(Orcamento.class));
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        Orcamento resultado = orcamentoService.criar(idOs, orcamento);

        assertNotNull(resultado);
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve atualizar status do orçamento para APROVADO com sucesso e deduzir estoque")
    void deveAtualizarStatusAprovadoComSucesso() {
        UUID idOrcamento = UUID.randomUUID();
        StatusOrcamento novoStatus = StatusOrcamento.APROVADO;

        Orcamento orcamento = criarOrcamentoMock();
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().plusDays(1));

        UUID idEstoque = UUID.randomUUID();
        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), null, 5, null, null, idEstoque, null, null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(), null, null, List.of(item), null
        );

        orcamento.setServicos(List.of(servico));

        Estoque estoque = new Estoque(
                idEstoque, "Óleo", null, null, 10, null, null
        );

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));
        doNothing().when(orcamentoValidator).validarAtualizacaoStatus(novoStatus);
        doNothing().when(orcamentoValidator).validarEstoqueDisponivel(orcamento);
        when(estoqueRepository.findById(item.getIdEstoque())).thenReturn(Optional.of(estoque));
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);

        Orcamento resultado = orcamentoService.atualizarStatus(idOrcamento, novoStatus);

        assertNotNull(resultado);
        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        verify(estoqueRepository, times(1)).save(estoque);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar status de orçamento que não está PENDENTE")
    void deveLancarExcecaoStatusNaoPendente() {
        UUID idOrcamento = UUID.randomUUID();
        StatusOrcamento novoStatus = StatusOrcamento.APROVADO;

        Orcamento orcamento = criarOrcamentoMock();
        orcamento.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, novoStatus));
    }

    @Test
    @DisplayName("Deve expirar orçamento se a data de expiração já passou ao atualizar status")
    void deveExpirarOrcamentoSePassadoDataExpiracao() {
        UUID idOrcamento = UUID.randomUUID();
        StatusOrcamento novoStatus = StatusOrcamento.APROVADO;

        Orcamento orcamento = spy(criarOrcamentoMock());
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().minusHours(1));

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));
        doNothing().when(orcamentoValidator).validarAtualizacaoStatus(novoStatus);

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, novoStatus));
        verify(orcamento, times(1)).expirar();
        verify(orcamentoExpiradoUseCase, times(1)).salvarOrcamentoExpirado(orcamento);
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos com sucesso")
    void deveListarTodosOsOrcamentosComSucesso() {
        Orcamento orcamento = criarOrcamentoMock();

        when(orcamentoRepository.findAll()).thenReturn(List.of(orcamento));

        List<Orcamento> resultado = orcamentoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID com sucesso")
    void deveBuscarOrcamentoPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

        Orcamento resultado = orcamentoService.buscarPorId(id);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Deve listar orçamentos por Ordem de Serviço com sucesso")
    void deveListarPorOrdemServicoComSucesso() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();

        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamento));

        List<Orcamento> resultado = orcamentoService.listarPorOrdemServico(idOs);

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar por Ordem de Serviço vazia")
    void deveLancarExcecaoListarPorOsVazia() {
        UUID idOs = UUID.randomUUID();
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of());

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.listarPorOrdemServico(idOs));
    }

    @Test
    @DisplayName("Deve deletar orçamento e seus itens com sucesso")
    void deveDeletarOrcamentoComSucesso() {
        UUID id = UUID.randomUUID();
        when(orcamentoRepository.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> orcamentoService.deletar(id));

        verify(orcamentoRepository, times(1)).deletarItensDiretosPorOrcamento(id);
        verify(orcamentoRepository, times(1)).deletarItensPorServicosDoOrcamento(id);
        verify(orcamentoRepository, times(1)).deletarServicosPorOrcamento(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar orçamento inexistente")
    void deveLancarExcecaoDeletarInexistente() {
        UUID id = UUID.randomUUID();
        when(orcamentoRepository.existsById(id)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.deletar(id));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar orçamento com Ordem de Serviço não encontrada")
    void deveLancarExcecaoCriarOsNaoEncontrada() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.criar(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar orçamento por ID inexistente")
    void deveLancarExcecaoBuscarPorIdInexistente() {
        UUID id = UUID.randomUUID();
        when(orcamentoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.buscarPorId(id));
    }
}