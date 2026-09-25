package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ServicoResponse;
import br.com.autoflow.adapters.inbound.mapper.ServicoMapper;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.inbound.servico.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class ServicoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CriarServicoUseCase criarServicoUseCase;

    @Mock
    private ListarServicosUseCase listarServicosUseCase;

    @Mock
    private BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;

    @Mock
    private AtualizarServicoUseCase atualizarServicoUseCase;

    @Mock
    private DeletarServicoUseCase deletarServicoUseCase;

    @Spy
    private ServicoMapper servicoMapper = Mappers.getMapper(ServicoMapper.class);

    @InjectMocks
    private ServicoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Servico criarDominioExemplo(UUID id) {
        return new Servico(id, "Troca de óleo", BigDecimal.valueOf(150), 90);
    }

    @Test
    void deveCriar() {
        UUID id = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Troca de óleo", BigDecimal.valueOf(150), 90);
        Servico dominio = criarDominioExemplo(id);

        when(criarServicoUseCase.criar(any(Servico.class))).thenReturn(dominio);

        ServicoResponse result = controller.criar(request);

        assertEquals("Troca de óleo", result.dsServico());
        verify(criarServicoUseCase).criar(any(Servico.class));
    }

    @Test
    void deveListarTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID id = UUID.randomUUID();
        Servico dominio = criarDominioExemplo(id);
        Page<Servico> pageDomain = new PageImpl<>(List.of(dominio), pageable, 1);

        when(listarServicosUseCase.listarTodos(pageable)).thenReturn(pageDomain);

        Page<ServicoResponse> result = controller.listarTodos(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Troca de óleo", result.getContent().get(0).dsServico());
        verify(listarServicosUseCase).listarTodos(pageable);
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Servico dominio = criarDominioExemplo(id);

        when(buscarServicoPorIdUseCase.buscarPorId(id)).thenReturn(dominio);

        ServicoResponse result = controller.buscarPorId(id);

        assertEquals(id, result.idServico());
        verify(buscarServicoPorIdUseCase).buscarPorId(id);
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Revisão completa", BigDecimal.valueOf(220), 120);
        Servico dominioAtualizado = new Servico(id, "Revisão completa", BigDecimal.valueOf(220), 120);

        when(atualizarServicoUseCase.atualizar(eq(id), any(Servico.class))).thenReturn(dominioAtualizado);

        ServicoResponse result = controller.atualizar(id, request);

        assertEquals("Revisão completa", result.dsServico());
        verify(atualizarServicoUseCase).atualizar(eq(id), any(Servico.class));
    }

    @Test
    void deveDeletar() {
        UUID id = UUID.randomUUID();

        controller.deletar(id);

        verify(deletarServicoUseCase).deletar(id);
    }
}