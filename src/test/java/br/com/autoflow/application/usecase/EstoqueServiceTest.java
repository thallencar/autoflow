package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
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

    @InjectMocks
    private EstoqueUseCaseImpl estoqueService;

    @Test
    @DisplayName("Deve criar item no estoque com sucesso.")
    void deveCriarItemNoEstoqueComSucesso() {
        // Arrange
        UUID idGerado = UUID.randomUUID();
        Estoque estoqueInput = new Estoque(null, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);
        Estoque estoqueSalvo = new Estoque(idGerado, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);

        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoqueSalvo);

        // Act
        Estoque resultado = estoqueService.criar(estoqueInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(idGerado, resultado.getId());
        assertEquals("Filtro de óleo", resultado.getNomeItem());
        assertEquals(TipoItemEstoque.INSUMO, resultado.getTipoCategoria());
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

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));

        // Act
        Estoque resultado = estoqueService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Filtro de óleo", resultado.getNomeItem());
        verify(estoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os itens de estoque com sucesso")
    void deveListarTodosOsItensDeEstoqueComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);

        when(estoqueRepository.findAll()).thenReturn(List.of(estoque));

        // Act
        List<Estoque> resultado = estoqueService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Filtro de óleo", resultado.get(0).getNomeItem());
        verify(estoqueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar valor unitário do estoque com sucesso")
    void deveAtualizarValorUnitarioComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoque = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoque);

        // Act
        Estoque resultado = estoqueService.atualizarValorUnitario(id, BigDecimal.valueOf(70));

        // Assert
        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(70), resultado.getValorUnitario());
        verify(estoqueRepository, times(1)).findById(id);
        verify(estoqueRepository, times(1)).save(estoque);
    }

    @Test
    @DisplayName("Deve atualizar item de estoque com sucesso")
    void deveAtualizarItemComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estoque estoqueExistente = new Estoque(id, "Filtro de óleo", "Tecfil", BigDecimal.valueOf(55), 35, 5, TipoItemEstoque.INSUMO);
        Estoque estoqueParam = new Estoque(null, "Filtro de Ar", "Bosch", BigDecimal.valueOf(60), 40, 10, TipoItemEstoque.PECA);

        when(estoqueRepository.findById(id)).thenReturn(Optional.of(estoqueExistente));
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoqueExistente);

        // Act
        Estoque resultado = estoqueService.atualizar(id, estoqueParam);

        // Assert
        assertNotNull(resultado);
        assertEquals("Filtro de Ar", resultado.getNomeItem());
        assertEquals("Bosch", resultado.getNomeMarca());
        verify(estoqueRepository, times(1)).findById(id);
        verify(estoqueRepository, times(1)).save(estoqueExistente);
    }
}