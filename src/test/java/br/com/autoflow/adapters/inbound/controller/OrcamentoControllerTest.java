package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoResponse;
import br.com.autoflow.adapters.inbound.mapper.OrcamentoMapper;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.ports.inbound.orcamento.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CriarOrcamentoUseCase criarOrcamentoUseCase;

    @Mock
    private AtualizarStatusOrcamentoUseCase atualizarStatusOrcamentoUseCase;

    @Mock
    private ListarOrcamentosUseCase listarOrcamentosUseCase;

    @Mock
    private BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase;

    @Mock
    private DeletarOrcamentoUseCase deletarOrcamentoUseCase;

    @Spy
    private OrcamentoMapper orcamentoMapper = Mappers.getMapper(OrcamentoMapper.class);

    @InjectMocks
    private OrcamentoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Orcamento criarDominioExemplo(UUID id, UUID idOs) {
        return new Orcamento(
                id, TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, List.of(), List.of()
        );
    }

    @Test
    void deveCriar() {
        UUID idOs = UUID.randomUUID();
        UUID idOrcamento = UUID.randomUUID();
        OrcamentoRequest request = new OrcamentoRequest(idOs, TipoOrcamento.INICIAL, LocalDateTime.now(), List.of(), List.of());
        Orcamento dominio = criarDominioExemplo(idOrcamento, idOs);

        when(criarOrcamentoUseCase.criar(eq(idOs), any(Orcamento.class))).thenReturn(dominio);

        OrcamentoResponse result = controller.criar(request);

        assertEquals(idOrcamento, result.id());
        verify(criarOrcamentoUseCase).criar(eq(idOs), any(Orcamento.class));
    }

    @Test
    void deveListarTodos() {
        UUID id = UUID.randomUUID();
        Orcamento dominio = criarDominioExemplo(id, UUID.randomUUID());
        when(listarOrcamentosUseCase.listarTodos()).thenReturn(List.of(dominio));

        List<OrcamentoResponse> result = controller.listarTodos();

        assertEquals(1, result.size());
        assertEquals(id, result.get(0).id());
        verify(listarOrcamentosUseCase).listarTodos();
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Orcamento dominio = criarDominioExemplo(id, UUID.randomUUID());
        when(buscarOrcamentoPorIdUseCase.buscarPorId(id)).thenReturn(dominio);

        OrcamentoResponse result = controller.buscarPorId(id);

        assertEquals(id, result.id());
        verify(buscarOrcamentoPorIdUseCase).buscarPorId(id);
    }

    @Test
    void deveDeletar() {
        UUID id = UUID.randomUUID();

        controller.delete(id);

        verify(deletarOrcamentoUseCase).deletar(id);
    }

    @Test
    void deveAtualizarStatus() {
        UUID id = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);
        Orcamento dominio = criarDominioExemplo(id, UUID.randomUUID());
        dominio.aprovar();

        when(atualizarStatusOrcamentoUseCase.atualizarStatus(eq(id), eq(StatusOrcamento.APROVADO))).thenReturn(dominio);

        OrcamentoResponse result = controller.atualizarStatus(id, request);

        assertEquals(StatusOrcamento.APROVADO, result.status());
        verify(atualizarStatusOrcamentoUseCase).atualizarStatus(eq(id), eq(StatusOrcamento.APROVADO));
    }

    @Test
    void deveListarPorOrdemServico() {
        UUID idOs = UUID.randomUUID();
        UUID idOrcamento = UUID.randomUUID();
        Orcamento dominio = criarDominioExemplo(idOrcamento, idOs);

        when(listarOrcamentosUseCase.listarPorOrdemServico(idOs)).thenReturn(List.of(dominio));

        List<OrcamentoResponse> result = controller.listarPorOrcamentoOrdemDeServico(idOs);

        assertEquals(1, result.size());
        assertEquals(idOrcamento, result.get(0).id());
        verify(listarOrcamentosUseCase).listarPorOrdemServico(idOs);
    }
}