package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrcamentoItemRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoServicoRequest;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.*;
import br.com.autoflow.domain.repository.EstoqueRepository;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoValidatorTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private ServicoValidator servicoValidator;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @InjectMocks
    private OrcamentoValidator validator;

    // --- TESTES DE CRIAÇÃO (Fluxo Principal e Tipos de Orçamento) ---

    @Test
    @DisplayName("Deve validar a criação de um orçamento INICIAL com sucesso")
    void deveValidarCriacaoOrcamentoInicialComSucesso() {
        UUID idOs = UUID.randomUUID();
        UUID idServico = UUID.randomUUID();
        UUID idEstoque = UUID.randomUUID();

        OrcamentoItemRequest itemRequest = new OrcamentoItemRequest(2, BigDecimal.valueOf(50.00), BigDecimal.valueOf(100.00), idEstoque);
        OrcamentoServicoRequest servicoRequest = new OrcamentoServicoRequest(idServico, BigDecimal.valueOf(150.00), List.of(itemRequest));

        OrcamentoRequest request = new OrcamentoRequest(
                idOs,
                TipoOrcamento.INICIAL,
                LocalDateTime.now().plusDays(5),
                List.of(servicoRequest),
                null
        );

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Estoque estoque = new Estoque();
        estoque.setQuantidadeEstoque(10);
        estoque.setNomeItem("Filtro de Óleo");

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());
        when(servicoValidator.buscarPorId(idServico)).thenReturn(new Servico());
        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertDoesNotThrow(() -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se tipo de orçamento for nulo")
    void deveLancarExcecaoTipoOrcamentoNulo() {
        OrcamentoRequest request = new OrcamentoRequest(UUID.randomUUID(), null, LocalDateTime.now().plusDays(1), null, null);
        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se ID da OS for nulo")
    void deveLancarExcecaoIdOsNulo() {
        OrcamentoRequest request = new OrcamentoRequest(null, TipoOrcamento.INICIAL, LocalDateTime.now().plusDays(1), null, null);
        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se Ordem de Serviço estiver com status restrito (Ex: Cancelada)")
    void deveLancarExcecaoOsCancelada() {
        UUID idOs = UUID.randomUUID();
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.INICIAL, LocalDateTime.now().plusDays(1), null, null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.CANCELADA);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar segundo orçamento INICIAL")
    void deveLancarExcecaoJaExisteInicial() {
        UUID idOs = UUID.randomUUID();
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.INICIAL, LocalDateTime.now().plusDays(1), null, null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Orcamento orcamentoExistente = new Orcamento();
        orcamentoExistente.setTipoOrcamento(TipoOrcamento.INICIAL);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamentoExistente));

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve validar a criação de um orçamento COMPLEMENTAR com sucesso")
    void deveValidarCriacaoOrcamentoComplementarComSucesso() {
        UUID idOs = UUID.randomUUID();
        UUID idServicoNovo = UUID.randomUUID();

        OrcamentoServicoRequest servicoRequest = new OrcamentoServicoRequest(idServicoNovo, BigDecimal.valueOf(100.00), null);
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.COMPLEMENTAR, null, List.of(servicoRequest), null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Orcamento orcamentoAnterior = new Orcamento();
        orcamentoAnterior.setTipoOrcamento(TipoOrcamento.INICIAL);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamentoAnterior));
        when(servicoValidator.buscarPorId(idServicoNovo)).thenReturn(new Servico());

        assertDoesNotThrow(() -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar COMPLEMENTAR sem orçamento anterior")
    void deveLancarExcecaoComplementarSemAnterior() {
        UUID idOs = UUID.randomUUID();
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.COMPLEMENTAR, null, null, null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se serviço do COMPLEMENTAR já foi adicionado em outro orçamento")
    void deveLancarExcecaoServicoDuplicadoEmComplementar() {
        UUID idOs = UUID.randomUUID();
        UUID idServico = UUID.randomUUID();

        OrcamentoServicoRequest servicoRequest = new OrcamentoServicoRequest(idServico, BigDecimal.valueOf(100.00), null);
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.COMPLEMENTAR, null, List.of(servicoRequest), null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Servico servicoModel = new Servico();
        servicoModel.setIdServico(idServico);

        OrcamentoServico orcamentoServicoAnterior = new OrcamentoServico();
        orcamentoServicoAnterior.setServico(servicoModel);

        Orcamento orcamentoAnterior = new Orcamento();
        orcamentoAnterior.setServicos(List.of(orcamentoServicoAnterior));

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamentoAnterior));

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    // --- TESTES DE VALIDAÇÃO DE DATA DE EXPIRAÇÃO ---

    @Test
    @DisplayName("Deve lançar exceção se data de expiração for nula em orçamento inicial")
    void deveLancarExcecaoDataExpiracaoNula() {
        UUID idOs = UUID.randomUUID();
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.INICIAL, null, List.of(mock(OrcamentoServicoRequest.class)), null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se data de expiração estiver no passado")
    void deveLancarExcecaoDataExpiracaoPassada() {
        UUID idOs = UUID.randomUUID();
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.INICIAL, LocalDateTime.now().minusDays(1), List.of(mock(OrcamentoServicoRequest.class)), null);

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    // --- TESTES DE STATUS E SERVIÇOS ---

    @Test
    @DisplayName("Deve validar alteração de status com sucesso para Aprovado ou Recusado")
    void deveValidarAtualizacaoStatusComSucesso() {
        assertDoesNotThrow(() -> validator.validarAtualizacaoStatus(StatusOrcamento.APROVADO));
        assertDoesNotThrow(() -> validator.validarAtualizacaoStatus(StatusOrcamento.RECUSADO));
    }

    @Test
    @DisplayName("Deve lançar exceção ao alterar para status não permitido")
    void deveLancarExcecaoStatusInvalido() {
        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoStatus(StatusOrcamento.PENDENTE));
    }

    @Test
    @DisplayName("Deve lançar exceção se lista de serviços estiver vazia ou nula")
    void deveLancarExcecaoServicosVazios() {
        assertThrows(RegraNegocioException.class, () -> validator.validarServicosRequest(null));
        assertThrows(RegraNegocioException.class, () -> validator.validarServicosRequest(Collections.emptyList()));
    }

    @Test
    @DisplayName("Deve lançar exceção se ID do serviço for nulo")
    void deveLancarExcecaoIdServicoNulo() {
        OrcamentoServicoRequest request = new OrcamentoServicoRequest(null, BigDecimal.valueOf(100.00), null);
        assertThrows(RegraNegocioException.class, () -> validator.validarEBuscarServico(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se mão de obra for menor ou igual a zero")
    void deveLancarExcecaoMaoDeObraInvalida() {
        OrcamentoServicoRequest reqZero = new OrcamentoServicoRequest(UUID.randomUUID(), BigDecimal.ZERO, null);
        OrcamentoServicoRequest reqNegativa = new OrcamentoServicoRequest(UUID.randomUUID(), BigDecimal.valueOf(-10), null);

        assertThrows(RegraNegocioException.class, () -> validator.validarEBuscarServico(reqZero));
        assertThrows(RegraNegocioException.class, () -> validator.validarEBuscarServico(reqNegativa));
    }

    // --- TESTES DE ESTOQUE E ITENS DO ORÇAMENTO ---

    @Test
    @DisplayName("Deve lançar exceção se quantidade em estoque for insuficiente para o item")
    void deveLancarExcecaoEstoqueInsuficiente() {
        UUID idServico = UUID.randomUUID();
        UUID idEstoque = UUID.randomUUID();

        OrcamentoItemRequest itemRequest = new OrcamentoItemRequest(15, BigDecimal.valueOf(10.00), BigDecimal.valueOf(10.00), idEstoque);
        OrcamentoServicoRequest servicoRequest = new OrcamentoServicoRequest(idServico, BigDecimal.valueOf(100.00), List.of(itemRequest));

        Estoque estoque = new Estoque();
        estoque.setQuantidadeEstoque(5);
        estoque.setNomeItem("Pastilha de Freio");

        when(servicoValidator.buscarPorId(idServico)).thenReturn(new Servico());
        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertThrows(RegraNegocioException.class, () -> validator.validarServicosRequest(List.of(servicoRequest)));
    }

    @Test
    @DisplayName("Deve validar estoque disponível a partir de um objeto Orçamento com sucesso")
    void deveValidarEstoqueDisponivelOrcamentoComSucesso() {
        UUID idEstoque = UUID.randomUUID();

        OrcamentoItem item = new OrcamentoItem();
        item.setIdEstoque(idEstoque);
        item.setQuantidade(2);

        OrcamentoServico servico = new OrcamentoServico();
        servico.setItens(List.of(item));

        Orcamento orcamento = new Orcamento();
        orcamento.setServicos(List.of(servico));

        Estoque estoque = new Estoque();
        estoque.setQuantidadeEstoque(10);

        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertDoesNotThrow(() -> validator.validarEstoqueDisponivel(orcamento));
    }
}