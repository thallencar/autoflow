package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.application.dto.EstoqueRequest;
import br.com.autoflow.application.dto.EstoqueResponse;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.repository.EstoqueRepository;
import br.com.autoflow.infrastructure.mapper.EstoqueMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any; // <-- Import correto do Mockito
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstoqueServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private EstoqueMapper estoqueMapper;

    @InjectMocks
    private EstoqueService estoqueService;

    @Test
    @DisplayName("Deve criar item no estoque com sucesso.")
    void deveCriarItemNoEstoqueComSucesso() {
        // 1. Cenário (Given) - Tudo dentro do método!
        EstoqueRequest request = new EstoqueRequest(
                "Filtro de óleo",
                "Tecfil",
                BigDecimal.valueOf(55),
                35,
                5,
                TipoItemEstoque.INSUMO
        );

        Estoque estoqueEntity = new Estoque();
        Estoque estoqueSalvo = new Estoque();
        UUID idGerado = UUID.randomUUID();

        // Ajuste caso o seu EstoqueResponse também use Enum no tipoCategoria
        EstoqueResponse responseEsperado = new EstoqueResponse(
                idGerado,
                "Filtro de óleo",
                "Tecfil",
                BigDecimal.valueOf(55),
                35,
                5,
                TipoItemEstoque.INSUMO
        );

        when(estoqueMapper.toEntity(request)).thenReturn(estoqueEntity);
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoqueSalvo);
        when(estoqueMapper.toResponse(estoqueSalvo)).thenReturn(responseEsperado);

        EstoqueResponse resultado = estoqueService.criar(request);

        assertNotNull(resultado);
        assertEquals("Filtro de óleo", resultado.nomeItem());
        assertEquals(TipoItemEstoque.INSUMO, resultado.tipoCategoria());

        // Garante que o método save foi chamado exatamente uma vez
        verify(estoqueRepository, times(1)).save(any(Estoque.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID do estoque não for encontrado")
    void buscarPorId_QuandoIdNaoExistir_DeveLancarExcecao() {
        UUID idInexistente = UUID.randomUUID();
        when(estoqueRepository.findById(idInexistente)).thenReturn(Optional.empty());

        RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
            estoqueService.buscarPorId(idInexistente);
        });

        assertNotNull(excecao.getMessage());

        verify(estoqueRepository, times(1)).findById(idInexistente);
    }
    @Test
    @DisplayName("Deve buscar item de estoque por ID com sucesso")
    void deveBuscarItemPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque();
        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO
        );

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        EstoqueResponse resultado = estoqueService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        verify(estoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os itens de estoque com sucesso")
    void deveListarTodosOsItensDeEstoqueComSucesso() {
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque();
        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO
        );

        when(estoqueRepository.findAll()).thenReturn(List.of(estoque));
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        List<EstoqueResponse> resultado = estoqueService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Filtro de óleo", resultado.get(0).nomeItem());
        verify(estoqueRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Deve atualizar valor unitário do estoque com sucesso")
    void deveAtualizarValorUnitarioComSucesso() {
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque();

        AtualizarValorEstoqueRequest request = new AtualizarValorEstoqueRequest(BigDecimal.valueOf(70));

        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(70), 35, 5, TipoItemEstoque.INSUMO
        );

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoque);
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        EstoqueResponse resultado = estoqueService.atualizarValorUnitario(id, request);

        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(70), resultado.valorUnitario());
        verify(estoqueRepository, times(1)).findById(id);
        verify(estoqueRepository, times(1)).save(estoque);
    }

    @Test
    @DisplayName("Deve atualizar item de estoque com sucesso")
    void deveAtualizarItemComSucesso() {
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque();

        EstoqueRequest request = new EstoqueRequest(
                "Filtro de Ar", "Bosch", BigDecimal.valueOf(60), 40, 10, TipoItemEstoque.PEÇA
        );

        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de Ar", "Bosch", BigDecimal.valueOf(60), 40, 10, TipoItemEstoque.PEÇA
        );

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoque);
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        EstoqueResponse resultado = estoqueService.atualizar(id, request);

        assertNotNull(resultado);
        assertEquals("Filtro de Ar", resultado.nomeItem());
        assertEquals("Bosch", resultado.nomeMarca());
        verify(estoqueRepository, times(1)).findById(id);
        verify(estoqueRepository, times(1)).save(estoque);
    }

}