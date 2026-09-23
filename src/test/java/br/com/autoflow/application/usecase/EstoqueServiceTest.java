package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueResponse;
import br.com.autoflow.adapters.inbound.mapper.EstoqueMapper;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepositoryPort estoqueRepository;

    @Mock
    private EstoqueMapper estoqueMapper;

    @InjectMocks
    private EstoqueUseCase estoqueService;

    @Test
    @DisplayName("Deve criar item no estoque com sucesso.")
    void deveCriarItemNoEstoqueComSucesso() {
        // Arrange
        EstoqueRequest request = new EstoqueRequest(
                "Filtro de óleo",
                "Tecfil",
                BigDecimal.valueOf(55),
                35,
                5,
                TipoItemEstoque.INSUMO
        );

        UUID idGerado = UUID.randomUUID();
        Estoque estoqueEntity = new Estoque(null, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);
        Estoque estoqueSalvo = new Estoque(idGerado, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);

        EstoqueResponse responseEsperado = new EstoqueResponse(
                idGerado,
                "Filtro de óleo",
                "Tecfil",
                BigDecimal.valueOf(55),
                35,
                5,
                TipoItemEstoque.INSUMO
        );

        when(estoqueMapper.toDomain(request)).thenReturn(estoqueEntity);
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoqueSalvo);
        when(estoqueMapper.toResponse(estoqueSalvo)).thenReturn(responseEsperado);

        // Act
        EstoqueResponse resultado = estoqueService.criar(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Filtro de óleo", resultado.nomeItem());
        assertEquals(TipoItemEstoque.INSUMO, resultado.tipoCategoria());
        verify(estoqueRepository, times(1)).save(any(Estoque.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID do estoque não for encontrado")
    void buscarPorId_QuandoIdNaoExistir_DeveLancarExcecao() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(estoqueRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
            estoqueService.buscarPorId(idInexistente);
        });

        assertNotNull(excecao.getMessage());
        verify(estoqueRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve buscar item de estoque por ID com sucesso")
    void deveBuscarItemPorIdComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);
        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO
        );

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        // Act
        EstoqueResponse resultado = estoqueService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        verify(estoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os itens de estoque com sucesso")
    void deveListarTodosOsItensDeEstoqueComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);
        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO
        );

        when(estoqueRepository.findAll()).thenReturn(List.of(estoque));
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        // Act
        List<EstoqueResponse> resultado = estoqueService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Filtro de óleo", resultado.get(0).nomeItem());
        verify(estoqueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar valor unitário do estoque com sucesso")
    void deveAtualizarValorUnitarioComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);

        AtualizarValorEstoqueRequest request = new AtualizarValorEstoqueRequest(BigDecimal.valueOf(70));

        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(70), 35, 5, TipoItemEstoque.INSUMO
        );

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoque);
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        // Act
        EstoqueResponse resultado = estoqueService.atualizarValorUnitario(id, request);

        // Assert
        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(70), resultado.valorUnitario());
        verify(estoqueRepository, times(1)).findById(id);
        verify(estoqueRepository, times(1)).save(estoque);
    }

    @Test
    @DisplayName("Deve atualizar item de estoque com sucesso")
    void deveAtualizarItemComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);

        EstoqueRequest request = new EstoqueRequest(
                "Filtro de Ar", "Bosch", BigDecimal.valueOf(60), 40, 10, TipoItemEstoque.PECA
        );

        EstoqueResponse response = new EstoqueResponse(
                id, "Filtro de Ar", "Bosch", BigDecimal.valueOf(60), 40, 10, TipoItemEstoque.PECA
        );

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoque);
        when(estoqueMapper.toResponse(estoque)).thenReturn(response);

        // Act
        EstoqueResponse resultado = estoqueService.atualizar(id, request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Filtro de Ar", resultado.nomeItem());
        assertEquals("Bosch", resultado.nomeMarca());
        verify(estoqueRepository, times(1)).findById(id);
        verify(estoqueRepository, times(1)).save(estoque);
    }
}