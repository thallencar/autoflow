package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.outbound.OrcamentoServicoRepositoryPort;
import br.com.autoflow.ports.outbound.OsServicoRepositoryPort;
import br.com.autoflow.ports.outbound.ServicoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoValidatorTest {

    @Mock
    private ServicoRepositoryPort servicoRepository;

    @Mock
    private OsServicoRepositoryPort osServicoRepository;

    @Mock
    private OrcamentoServicoRepositoryPort orcamentoServicoRepository;

    @InjectMocks
    private br.com.autoflow.application.service.ServicoValidator validator;


    @Test
    @DisplayName("Deve validar criação de serviço com sucesso quando descrição não estiver duplicada")
    void deveValidarCriacaoComSucesso() {
        ServicoRequest request = new ServicoRequest("Alinhamento", BigDecimal.valueOf(100.00), 30);

        when(servicoRepository.existsByDsServicoIgnoreCase("Alinhamento")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarCriacao(request));
        verify(servicoRepository, times(1)).existsByDsServicoIgnoreCase("Alinhamento");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar serviço com descrição já existente")
    void deveLancarExcecaoCriacaoDescricaoDuplicada() {
        ServicoRequest request = new ServicoRequest("Alinhamento", BigDecimal.valueOf(100.00), 30);

        when(servicoRepository.existsByDsServicoIgnoreCase("Alinhamento")).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request));
    }

    // --- TESTES DE ATUALIZAÇÃO ---

    @Test
    @DisplayName("Deve validar atualização de serviço com sucesso quando descrição não existe ou é do mesmo ID")
    void deveValidarAtualizacaoComSucesso() {
        UUID id = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Balanceamento", BigDecimal.valueOf(80.00), 30);

        Servico servicoExistenteComMesmoId = new Servico(id, "Balanceamento", BigDecimal.valueOf(80.00), 30);

        when(servicoRepository.existsById(id)).thenReturn(true);
        // Simula encontrar a mesma descrição, mas pertencente ao ID que está sendo atualizado (permite a edição)
        when(servicoRepository.findByDsServicoIgnoreCase("Balanceamento")).thenReturn(Optional.of(servicoExistenteComMesmoId));

        assertDoesNotThrow(() -> validator.validarAtualizacao(id, request));
    }

    @Test
    @DisplayName("Deve lançar exceção se serviço não existir ao tentar atualizar")
    void deveLancarExcecaoAtualizacaoServicoInexistente() {
        UUID id = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Balanceamento", BigDecimal.valueOf(80.00), 30);

        when(servicoRepository.existsById(id)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarAtualizacao(id, request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar para uma descrição que já pertence a outro serviço")
    void deveLancarExcecaoAtualizacaoDescricaoConflitante() {
        UUID idCorrente = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Troca de Óleo", BigDecimal.valueOf(150.00), 30);

        Servico outroServico = new Servico(outroId, "Troca de Óleo", BigDecimal.valueOf(150.00), 30);

        when(servicoRepository.existsById(idCorrente)).thenReturn(true);
        when(servicoRepository.findByDsServicoIgnoreCase("Troca de Óleo")).thenReturn(Optional.of(outroServico));

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacao(idCorrente, request));
    }

    // --- TESTES DE BUSCA POR ID ---

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Servico servico = new Servico(id, "Alinhamento", BigDecimal.valueOf(100.00), 30);

        when(servicoRepository.findById(id)).thenReturn(Optional.of(servico));

        Servico resultado = validator.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(servico, resultado);
    }

    @Test
    @DisplayName("Deve lançar exceção se serviço não for encontrado por ID")
    void deveLancarExcecaoBuscarPorIdInexistente() {
        UUID id = UUID.randomUUID();

        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.buscarPorId(id));
    }

    // --- TESTES DE EXCLUSÃO ---

    @Test
    @DisplayName("Deve validar exclusão de serviço com sucesso")
    void deveValidarExclusaoComSucesso() {
        UUID id = UUID.randomUUID();

        when(servicoRepository.existsById(id)).thenReturn(true);
        when(osServicoRepository.existsByServico_IdServico(id)).thenReturn(false);
        when(orcamentoServicoRepository.existsByServico_IdServico(id)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarExclusao(id));
    }

    @Test
    @DisplayName("Deve lançar exceção se tentar excluir serviço inexistente")
    void deveLancarExcecaoExclusaoInexistente() {
        UUID id = UUID.randomUUID();

        when(servicoRepository.existsById(id)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarExclusao(id));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir serviço vinculado a uma Ordem de Serviço")
    void deveLancarExcecaoExclusaoVinculadoOs() {
        UUID id = UUID.randomUUID();

        when(servicoRepository.existsById(id)).thenReturn(true);
        when(osServicoRepository.existsByServico_IdServico(id)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> validator.validarExclusao(id));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir serviço vinculado a um Orçamento")
    void deveLancarExcecaoExclusaoVinculadoOrcamento() {
        UUID id = UUID.randomUUID();

        when(servicoRepository.existsById(id)).thenReturn(true);
        when(osServicoRepository.existsByServico_IdServico(id)).thenReturn(false);
        when(orcamentoServicoRepository.existsByServico_IdServico(id)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> validator.validarExclusao(id));
    }
}