package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoResponse;
import br.com.autoflow.adapters.inbound.mapper.OrcamentoMapper;
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
class OrcamentoUseCaseTest {

    @Mock
    private OrcamentoRepositoryPort orcamentoRepository;

    @Mock
    private OrcamentoMapper orcamentoMapper;

    @Mock
    private OrdemServicoRepositoryPort ordemServicoRepository;

    @Mock
    private OrcamentoExpiradoUseCase orcamentoExpiradoUseCase;

    @Mock
    private EstoqueRepositoryPort estoqueRepository;

    @InjectMocks
    private OrcamentoUseCase orcamentoService;

    private Orcamento criarOrcamentoMock() {
        return new Orcamento(
                UUID.randomUUID(), null, StatusOrcamento.PENDENTE,
                null, null, null, null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("Deve criar um orçamento inicial com sucesso")
    void deveCriarOrcamentoComSucesso() {
        OrcamentoRequest request = mock(OrcamentoRequest.class);
        Orcamento orcamento = criarOrcamentoMock();
        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);
        UUID idOs = UUID.randomUUID();

        when(request.idOs()).thenReturn(idOs);
        when(request.tipoOrcamento()).thenReturn(null);
        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(new OrdemServico()));
        when(orcamentoMapper.toEntity(request)).thenReturn(orcamento);
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        OrcamentoResponse resultado = orcamentoService.criar(request);

        assertNotNull(resultado);
        verify(orcamentoMapper, times(1)).toEntity(request);
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve criar um orçamento complementar com sucesso quando houver orçamento aprovado")
    void deveCriarOrcamentoComplementarComSucesso() {
        OrcamentoRequest request = mock(OrcamentoRequest.class);
        UUID idOs = UUID.randomUUID();
        TipoOrcamento tipoOrcamento = TipoOrcamento.COMPLEMENTAR;

        Orcamento orcamento = criarOrcamentoMock();
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setStatusOS(StatusOS.EM_EXECUCAO);

        Orcamento orcamentoAprovado = criarOrcamentoMock();
        orcamentoAprovado.setStatus(StatusOrcamento.APROVADO);
        ordemServico.getIdsOrcamento().add(orcamentoAprovado);

        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(request.idOs()).thenReturn(idOs);
        when(request.tipoOrcamento()).thenReturn(tipoOrcamento);
        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(ordemServico));
        when(orcamentoMapper.toEntity(request)).thenReturn(orcamento);
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        OrcamentoResponse resultado = orcamentoService.criar(request);

        assertNotNull(resultado);
        verify(ordemServicoRepository, times(1)).save(ordemServico);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar orçamento complementar sem orçamento inicial aprovado")
    void deveLancarExcecaoCriarComplementarSemAprovado() {
        OrcamentoRequest request = mock(OrcamentoRequest.class);
        UUID idOs = UUID.randomUUID();
        TipoOrcamento tipoOrcamento = TipoOrcamento.COMPLEMENTAR;

        Orcamento orcamento = criarOrcamentoMock();
        OrdemServico ordemServico = new OrdemServico();

        when(request.idOs()).thenReturn(idOs);
        when(request.tipoOrcamento()).thenReturn(tipoOrcamento);
        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(ordemServico));
        when(orcamentoMapper.toEntity(request)).thenReturn(orcamento);

        assertThrows(RegraNegocioException.class, () -> orcamentoService.criar(request));
    }

    @Test
    @DisplayName("Deve atualizar status do orçamento para APROVADO com sucesso e deduzir estoque")
    void deveAtualizarStatusAprovadoComSucesso() {
        UUID idOrcamento = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);

        Orcamento orcamento = criarOrcamentoMock();
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().plusDays(1));

        UUID idEstoque = UUID.randomUUID();
        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                null,
                15,
                null,
                null,
                idEstoque,
                null,
                null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(),
                null,
                null,
                List.of(item),
                null
        );

        orcamento.setServicos(List.of(servico));

        Estoque estoque = new Estoque(
                idEstoque,
                "Óleo",
                null,
                null,
                10, // Apenas 10 em estoque
                null,
                null
        );

        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));
        when(estoqueRepository.findById(item.getIdEstoque())).thenReturn(Optional.of(estoque));
        when(orcamentoRepository.save(any(Orcamento.class))).thenReturn(orcamento);
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        OrcamentoResponse resultado = orcamentoService.atualizarStatus(idOrcamento, request);

        assertNotNull(resultado);
        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        verify(estoqueRepository, times(1)).save(estoque);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar status de orçamento que não está PENDENTE")
    void deveLancarExcecaoStatusNaoPendente() {
        UUID idOrcamento = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);

        Orcamento orcamento = criarOrcamentoMock();
        orcamento.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, request));
    }

    @Test
    @DisplayName("Deve expirar orçamento se a data de expiração já passou ao atualizar status")
    void deveExpirarOrcamentoSePassadoDataExpiracao() {
        UUID idOrcamento = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);

        Orcamento orcamento = spy(criarOrcamentoMock());
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().minusHours(1));

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, request));
        verify(orcamento, times(1)).expirar();
        verify(orcamentoExpiradoUseCase, times(1)).salvarOrcamentoExpirado(orcamento);
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos com sucesso")
    void deveListarTodosOsOrcamentosComSucesso() {
        Orcamento orcamento = criarOrcamentoMock();
        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(orcamentoRepository.findAll()).thenReturn(List.of(orcamento));
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        List<OrcamentoResponse> resultado = orcamentoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID com sucesso")
    void deveBuscarOrcamentoPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();
        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        OrcamentoResponse resultado = orcamentoService.buscarPorId(id);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Deve listar orçamentos por Ordem de Serviço com sucesso")
    void deveListarPorOrdemServicoComSucesso() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = criarOrcamentoMock();
        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamento));
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        List<OrcamentoResponse> resultado = orcamentoService.listarPorOrdemServico(idOs);

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

        assertDoesNotThrow(() -> orcamentoService.delete(id));

        verify(orcamentoRepository, times(1)).deletarItensDiretosPorOrcamento(id);
        verify(orcamentoRepository, times(1)).deletarItensPorServicosDoOrcamento(id);
        verify(orcamentoRepository, times(1)).deletarServicosPorOrcamento(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar orçamento inexistente")
    void deveLancarExcecaoDeletarInexistente() {
        UUID id = UUID.randomUUID();
        when(orcamentoRepository.existsById(id)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.delete(id));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar orçamento com Ordem de Serviço não encontrada")
    void deveLancarExcecaoCriarOsNaoEncontrada() {
        OrcamentoRequest request = mock(OrcamentoRequest.class);
        UUID idOs = UUID.randomUUID();

        when(request.idOs()).thenReturn(idOs);
        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.criar(request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao aprovar orçamento com estoque insuficiente")
    void deveLancarExcecaoEstoqueInsuficiente() {
        UUID idOrcamento = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);

        Orcamento orcamento = criarOrcamentoMock();
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().plusDays(1));

        UUID idEstoque = UUID.randomUUID();

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                null,
                15,
                null,
                null,
                idEstoque,
                null,
                null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(),
                null,
                null,
                List.of(item),
                null
        );

        orcamento.setServicos(List.of(servico));

        Estoque estoque = new Estoque(
                idEstoque,
                "Óleo",
                null,
                null,
                10, // Apenas 10 em estoque
                null,
                null
        );

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));
        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar orçamento por ID inexistente")
    void deveLancarExcecaoBuscarPorIdInexistente() {
        UUID id = UUID.randomUUID();
        when(orcamentoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.buscarPorId(id));
    }
}