package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.OrcamentoValidator;
import br.com.autoflow.application.validator.ServicoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.*;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
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
    private OrdemServicoRepositoryPort ordemServicoRepository;

    @Mock
    private EstoqueRepositoryPort estoqueRepository;

    @Mock
    private ServicoValidator servicoValidator;

    @Mock
    private OrcamentoRepositoryPort orcamentoRepository;

    @InjectMocks
    private OrcamentoValidator validator;

    // --- TESTES DE CRIAÇÃO (Fluxo Principal e Tipos de Orçamento) ---

    @Test
    @DisplayName("Deve validar a criação de um orçamento INICIAL com sucesso")
    void deveValidarCriacaoOrcamentoInicialComSucesso() {
        UUID idOs = UUID.randomUUID();
        UUID idServico = UUID.randomUUID();
        UUID idEstoque = UUID.randomUUID();

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), null, 2, BigDecimal.valueOf(50.00), BigDecimal.valueOf(100.00), idEstoque, null, null
        );

        Servico servicoModel = new Servico(idServico, "Servico Exemplo", BigDecimal.valueOf(150.00), 60);

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(), BigDecimal.valueOf(150.00), servicoModel, List.of(item), null
        );

        Orcamento orcamento = new Orcamento(
                null, TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(5), null,
                BigDecimal.valueOf(100.00), BigDecimal.valueOf(150.00), BigDecimal.valueOf(250.00),
                null, List.of(servico), List.of(item)
        );

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Estoque estoque = new Estoque(
                idEstoque, "Filtro de Óleo", "Marca", BigDecimal.valueOf(50.00), 10, 2, TipoItemEstoque.PECA
        );

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());
        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertDoesNotThrow(() -> validator.validarCriacao(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve lançar exceção se tipo de orçamento for nulo")
    void deveLancarExcecaoTipoOrcamentoNulo() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = new Orcamento(
                null, null, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, Collections.emptyList(), Collections.emptyList()
        );

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve lançar exceção se ID da OS não for encontrado")
    void deveLancarExcecaoIdOsNaoEncontrado() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = new Orcamento();

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarCriacao(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve lançar exceção se Ordem de Serviço estiver com status restrito (Ex: Cancelada)")
    void deveLancarExcecaoOsCancelada() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = new Orcamento();

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.CANCELADA);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar segundo orçamento INICIAL")
    void deveLancarExcecaoJaExisteInicial() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = new Orcamento(
                null, TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, Collections.emptyList(), Collections.emptyList()
        );

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Orcamento orcamentoExistente = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(5), null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, Collections.emptyList(), Collections.emptyList()
        );

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamentoExistente));

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve validar a criação de um orçamento COMPLEMENTAR com sucesso")
    void deveValidarCriacaoOrcamentoComplementarComSucesso() {
        UUID idOs = UUID.randomUUID();
        UUID idServicoNovo = UUID.randomUUID();

        Servico servicoModel = new Servico(idServicoNovo, "Servico Novo", BigDecimal.valueOf(100.00), 30);
        OrcamentoServico servicoReq = new OrcamentoServico(UUID.randomUUID(), BigDecimal.valueOf(100.00), servicoModel, Collections.emptyList(), null);

        Orcamento orcamento = new Orcamento(
                null, TipoOrcamento.COMPLEMENTAR, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), null, null,
                BigDecimal.ZERO, BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00),
                null, List.of(servicoReq), Collections.emptyList()
        );

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        Orcamento orcamentoAnterior = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.APROVADO,
                LocalDateTime.now(), LocalDateTime.now().plusDays(5), null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, Collections.emptyList(), Collections.emptyList()
        );

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(List.of(orcamentoAnterior));

        assertDoesNotThrow(() -> validator.validarCriacao(idOs, orcamento));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar COMPLEMENTAR sem orçamento anterior")
    void deveLancarExcecaoComplementarSemAnterior() {
        UUID idOs = UUID.randomUUID();
        Orcamento orcamento = new Orcamento(
                null, TipoOrcamento.COMPLEMENTAR, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, Collections.emptyList(), Collections.emptyList()
        );

        OrdemServico os = new OrdemServico();
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        when(ordemServicoRepository.findById(idOs)).thenReturn(Optional.of(os));
        when(orcamentoRepository.findByOrdemServicoIdOs(idOs)).thenReturn(Collections.emptyList());

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(idOs, orcamento));
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

    // --- TESTES DE ESTOQUE E ITENS DO ORÇAMENTO ---

    @Test
    @DisplayName("Deve lançar exceção se quantidade em estoque for insuficiente")
    void deveLancarExcecaoEstoqueInsuficiente() {
        UUID idEstoque = UUID.randomUUID();

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), null, 15, BigDecimal.valueOf(10.00), BigDecimal.valueOf(150.00), idEstoque, null, null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(), BigDecimal.valueOf(100.00), new Servico(UUID.randomUUID(), "Servico", BigDecimal.valueOf(100), 30), List.of(item), null
        );

        Orcamento orcamento = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(5), null,
                BigDecimal.valueOf(150.00), BigDecimal.valueOf(100.00), BigDecimal.valueOf(250.00),
                null, List.of(servico), List.of(item)
        );

        Estoque estoque = new Estoque(
                idEstoque, "Pastilha de Freio", "Marca", BigDecimal.valueOf(10.00), 5, 2, TipoItemEstoque.PECA
        );

        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertThrows(RegraNegocioException.class, () -> validator.validarEstoqueDisponivel(orcamento));
    }

    @Test
    @DisplayName("Deve validar estoque disponível a partir de um objeto Orçamento com sucesso")
    void deveValidarEstoqueDisponivelOrcamentoComSucesso() {
        UUID idEstoque = UUID.randomUUID();

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), null, 2, BigDecimal.valueOf(10.00), BigDecimal.valueOf(20.00), idEstoque, null, null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(), BigDecimal.valueOf(50.00), null, List.of(item), null
        );

        Orcamento orcamento = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(5), null,
                BigDecimal.valueOf(20.00), BigDecimal.valueOf(50.00), BigDecimal.valueOf(70.00),
                null, List.of(servico), List.of(item)
        );

        Estoque estoque = new Estoque(
                idEstoque, "Item", "Marca", BigDecimal.valueOf(10.00), 10, 2, TipoItemEstoque.PECA
        );

        when(estoqueRepository.findById(idEstoque)).thenReturn(Optional.of(estoque));

        assertDoesNotThrow(() -> validator.validarEstoqueDisponivel(orcamento));
    }
}