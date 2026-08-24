package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.infrastructure.mapper.OrcamentoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private OrcamentoValidator orcamentoValidator;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private OrcamentoService orcamentoService;

    @Test
    @DisplayName("Deve criar um orçamento com sucesso")
    void deveCriarOrcamentoComSucesso() {
        // Cenário
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

        // Ação
        OrcamentoResponse resultado = orcamentoService.criar(request);

        // Verificação
        assertNotNull(resultado);
        verify(orcamentoMapper, times(1)).toEntity(request);
        verify(orcamentoRepository, times(1)).save(any(Orcamento.class));
        verify(orcamentoMapper, times(1)).toResponse(orcamento);
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos com sucesso")
    void deveListarTodosOsOrcamentosComSucesso() {
        // Cenário
        Orcamento orcamento = new Orcamento();
        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(orcamentoRepository.findAll()).thenReturn(List.of(orcamento));
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        // Ação
        List<OrcamentoResponse> resultado = orcamentoService.listarTodos();

        // Verificação
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(orcamentoRepository, times(1)).findAll();
        verify(orcamentoMapper, times(1)).toResponse(orcamento);
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID com sucesso")
    void deveBuscarOrcamentoPorIdComSucesso() {
        // Cenário
        UUID id = UUID.randomUUID();
        Orcamento orcamento = new Orcamento();
        OrcamentoResponse responseEsperado = mock(OrcamentoResponse.class);

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));
        when(orcamentoMapper.toResponse(orcamento)).thenReturn(responseEsperado);

        // Ação
        OrcamentoResponse resultado = orcamentoService.buscarPorId(id);

        // Verificação
        assertNotNull(resultado);
        verify(orcamentoRepository, times(1)).findById(id);
        verify(orcamentoMapper, times(1)).toResponse(orcamento);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID do orçamento não for encontrado")
    void buscarPorId_QuandoIdNaoExistir_DeveLancarExcecao() {
        // Cenário
        UUID idInexistente = UUID.randomUUID();
        when(orcamentoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação & Verificação
        RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
            orcamentoService.buscarPorId(idInexistente);
        });

        assertNotNull(excecao.getMessage());
        assertTrue(excecao.getMessage().contains("Orçamento com ID"));
        verify(orcamentoRepository, times(1)).findById(idInexistente);
    }
}