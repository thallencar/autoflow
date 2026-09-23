package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AdicionarEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueResponse;
import br.com.autoflow.application.usecase.EstoqueUseCase;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueControllerTest {

    @Mock
    private EstoqueUseCase estoqueService;

    @InjectMocks
    private EstoqueController controller;

    @Test
    void deveCriarItem() {
        EstoqueRequest request = new EstoqueRequest("Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        EstoqueResponse response = new EstoqueResponse(UUID.randomUUID(), "Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        when(estoqueService.criar(request)).thenReturn(response);

        var result = controller.criar(request);

        assertEquals(response, result.getBody());
        verify(estoqueService).criar(request);
    }

    @Test
    void deveListarTodos() {
        EstoqueResponse response = new EstoqueResponse(UUID.randomUUID(), "Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        when(estoqueService.listarTodos()).thenReturn(List.of(response));

        var result = controller.listarTodos();

        assertEquals(List.of(response), result.getBody());
        verify(estoqueService).listarTodos();
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        EstoqueResponse response = new EstoqueResponse(id, "Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        when(estoqueService.buscarPorId(id)).thenReturn(response);

        var result = controller.buscarPorId(id);

        assertEquals(response, result.getBody());
        verify(estoqueService).buscarPorId(id);
    }

    @Test
    void deveAdicionarQuantidade() {
        UUID id = UUID.randomUUID();
        AdicionarEstoqueRequest request = new AdicionarEstoqueRequest(5);
        EstoqueResponse response = new EstoqueResponse(id, "Filtro", "Bosch", BigDecimal.TEN, 15, 2, TipoItemEstoque.INSUMO);
        when(estoqueService.adicionarQuantidade(id, request)).thenReturn(response);

        var result = controller.adicionarQuantidade(id, request);

        assertEquals(response, result.getBody());
        verify(estoqueService).adicionarQuantidade(id, request);
    }

    @Test
    void deveAtualizarValorUnitario() {
        UUID id = UUID.randomUUID();
        AtualizarValorEstoqueRequest request = new AtualizarValorEstoqueRequest(BigDecimal.valueOf(25));
        EstoqueResponse response = new EstoqueResponse(id, "Filtro", "Bosch", BigDecimal.valueOf(25), 10, 2, TipoItemEstoque.INSUMO);
        when(estoqueService.atualizarValorUnitario(id, request)).thenReturn(response);

        var result = controller.atualizarValorUnitario(id, request);

        assertEquals(response, result.getBody());
        verify(estoqueService).atualizarValorUnitario(id, request);
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        EstoqueRequest request = new EstoqueRequest("Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        EstoqueResponse response = new EstoqueResponse(id, "Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        when(estoqueService.atualizar(id, request)).thenReturn(response);

        var result = controller.atualizar(id, request);

        assertEquals(response, result.getBody());
        verify(estoqueService).atualizar(id, request);
    }
}
