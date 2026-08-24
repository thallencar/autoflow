package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.*;
import br.com.autoflow.domain.repository.EstoqueRepository;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrcamentoMapper;
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
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private OrcamentoMapper orcamentoMapper;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private OrcamentoExpiradoService orcamentoExpiradoService;

    @Mock
    private EstoqueRepository estoqueRepository;

    @InjectMocks
    private OrcamentoService orcamentoService;

    @Test
    @DisplayName("Deve criar um orçamento inicial com sucesso")
    void deveCriarOrcamentoComSucesso() {
        OrcamentoRequest request = mock(OrcamentoRequest.class);
        Orcamento orcamento = new Orcamento();
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

        Orcamento orcamento = new Orcamento();
        OrdemServico ordemServico = new OrdemServico();

        ordemServico.setStatusOS(StatusOS.EM_EXECUCAO);

        Orcamento orcamentoAprovado = new Orcamento();
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

        Orcamento orcamento = new Orcamento();
        OrdemServico ordemServico = new OrdemServico(); // Sem orçamento aprovado

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

        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().plusDays(1));

        OrcamentoItem item = new OrcamentoItem();
        item.setIdEstoque(UUID.randomUUID());
        item.setQuantidade(2);

        OrcamentoServico servico = new OrcamentoServico();
        servico.setItens(List.of(item));
        orcamento.setServicos(List.of(servico));

        Estoque estoque = new Estoque();
        estoque.setQuantidadeEstoque(10);
        estoque.setNomeItem("Óleo");

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

        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.APROVADO); // Já aprovado

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, request));
    }

    @Test
    @DisplayName("Deve expirar orçamento se a data de expiração já passou ao atualizar status")
    void deveExpirarOrcamentoSePassadoDataExpiracao() {
        UUID idOrcamento = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);

        Orcamento orcamento = spy(new Orcamento());
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataExpiracao(LocalDateTime.now().minusHours(1)); // Expirado

        when(orcamentoRepository.findById(idOrcamento)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> orcamentoService.atualizarStatus(idOrcamento, request));
        verify(orcamento, times(1)).expirar();
        verify(orcamentoExpiradoService, times(1)).salvarOrcamentoExpirado(orcamento);
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos com sucesso")
    void deveListarTodosOsOrcamentosComSucesso() {
        Orcamento orcamento = new Orcamento();
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
        Orcamento orcamento = new Orcamento();
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
        Orcamento orcamento = new Orcamento();
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
}