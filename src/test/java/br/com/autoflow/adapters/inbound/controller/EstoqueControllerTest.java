package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AdicionarEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueResponse;
import br.com.autoflow.adapters.inbound.mapper.EstoqueMapper;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.ports.inbound.estoque.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CriarEstoqueUseCase criarEstoqueUseCase;

    @Mock
    private ListarEstoqueUseCase listarEstoqueUseCase;

    @Mock
    private BuscarEstoquePorIdUseCase buscarEstoquePorIdUseCase;

    @Mock
    private AtualizarEstoqueUseCase atualizarEstoqueUseCase;

    @Spy
    private EstoqueMapper estoqueMapper = Mappers.getMapper(EstoqueMapper.class);

    @InjectMocks
    private EstoqueController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Estoque criarDominioExemplo(UUID id) {
        return new Estoque(id, "Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
    }

    @Test
    void deveCriarItem() {
        UUID id = UUID.randomUUID();
        EstoqueRequest request = new EstoqueRequest("Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        Estoque dominio = criarDominioExemplo(id);

        when(criarEstoqueUseCase.criar(any(Estoque.class))).thenReturn(dominio);

        EstoqueResponse result = controller.criar(request);

        assertEquals("Filtro", result.nomeItem());
        verify(criarEstoqueUseCase).criar(any(Estoque.class));
    }

    @Test
    void deveListarTodos() {
        UUID id = UUID.randomUUID();
        Estoque dominio = criarDominioExemplo(id);
        when(listarEstoqueUseCase.listarTodos()).thenReturn(List.of(dominio));

        List<EstoqueResponse> result = controller.listarTodos();

        assertEquals(1, result.size());
        assertEquals("Filtro", result.get(0).nomeItem());
        verify(listarEstoqueUseCase).listarTodos();
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Estoque dominio = criarDominioExemplo(id);
        when(buscarEstoquePorIdUseCase.buscarPorId(id)).thenReturn(dominio);

        EstoqueResponse result = controller.buscarPorId(id);

        assertEquals(id, result.id());
        verify(buscarEstoquePorIdUseCase).buscarPorId(id);
    }

    @Test
    void deveAdicionarQuantidade() {
        UUID id = UUID.randomUUID();
        AdicionarEstoqueRequest request = new AdicionarEstoqueRequest(5);
        Estoque dominio = new Estoque(id, "Filtro", "Bosch", BigDecimal.TEN, 15, 2, TipoItemEstoque.INSUMO);

        when(atualizarEstoqueUseCase.adicionarQuantidade(eq(id), eq(5))).thenReturn(dominio);

        EstoqueResponse result = controller.adicionarQuantidade(id, request);

        assertEquals(15, result.quantidadeEstoque());
        verify(atualizarEstoqueUseCase).adicionarQuantidade(id, 5);
    }

    @Test
    void deveAtualizarValorUnitario() {
        UUID id = UUID.randomUUID();
        AtualizarValorEstoqueRequest request = new AtualizarValorEstoqueRequest(BigDecimal.valueOf(25));
        Estoque dominio = new Estoque(id, "Filtro", "Bosch", BigDecimal.valueOf(25), 10, 2, TipoItemEstoque.INSUMO);

        when(atualizarEstoqueUseCase.atualizarValorUnitario(eq(id), eq(BigDecimal.valueOf(25)))).thenReturn(dominio);

        EstoqueResponse result = controller.atualizarValorUnitario(id, request);

        assertEquals(BigDecimal.valueOf(25), result.valorUnitario());
        verify(atualizarEstoqueUseCase).atualizarValorUnitario(id, BigDecimal.valueOf(25));
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        EstoqueRequest request = new EstoqueRequest("Filtro", "Bosch", BigDecimal.TEN, 10, 2, TipoItemEstoque.INSUMO);
        Estoque dominio = criarDominioExemplo(id);

        when(atualizarEstoqueUseCase.atualizar(eq(id), any(Estoque.class))).thenReturn(dominio);

        EstoqueResponse result = controller.atualizar(id, request);

        assertEquals("Filtro", result.nomeItem());
        verify(atualizarEstoqueUseCase).atualizar(eq(id), any(Estoque.class));
    }
}