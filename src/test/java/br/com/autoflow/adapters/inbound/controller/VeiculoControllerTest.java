package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;
import br.com.autoflow.adapters.inbound.mapper.VeiculoMapper;
import br.com.autoflow.adapters.inbound.controller.exception.GlobalExceptionHandler;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.ports.inbound.veiculo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VeiculoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CriarVeiculoUseCase criarVeiculoUseCase;

    @Mock
    private ListarVeiculosUseCase listarVeiculosUseCase;

    @Mock
    private BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;

    @Mock
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;

    @Mock
    private DeletarVeiculoUseCase deletarVeiculoUseCase;

    @Spy
    private VeiculoMapper veiculoMapper = Mappers.getMapper(VeiculoMapper.class);

    @InjectMocks
    private VeiculoController veiculoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(veiculoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Veiculo criarDominioExemplo(UUID id, UUID clienteId) {
        return new Veiculo(
                id, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2022"), "Prata", clienteId
        );
    }

    @Test
    @DisplayName("Deve retornar HTTP 201 Created ao criar veículo válido")
    void deveCriarVeiculoComSucesso() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        VeiculoRequest request = new VeiculoRequest(
                "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2022"), "Prata", clienteId
        );
        Veiculo dominio = criarDominioExemplo(veiculoId, clienteId);

        when(criarVeiculoUseCase.criar(any(Veiculo.class))).thenReturn(dominio);

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"))
                .andExpect(jsonPath("$.anoFabricacao").value(2022))
                .andExpect(jsonPath("$.cor").value("Prata"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 Not Found quando veículo não existir")
    void deveRetornarNotFoundQuandoVeiculoNaoExistir() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(buscarVeiculoPorIdUseCase.buscarPorId(idInexistente))
                .thenThrow(new EntidadeNaoEncontradaException("Veículo", idInexistente));

        mockMvc.perform(get("/veiculos/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar HTTP 200 OK ao buscar veículo por ID existente")
    void deveBuscarVeiculoPorIdComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Veiculo dominio = criarDominioExemplo(id, clienteId);

        when(buscarVeiculoPorIdUseCase.buscarPorId(id)).thenReturn(dominio);

        mockMvc.perform(get("/veiculos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.marca").value("Toyota"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 200 OK ao listar todos os veículos")
    void deveListarTodosOsVeiculosComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Veiculo dominio = criarDominioExemplo(id, clienteId);

        when(listarVeiculosUseCase.listar()).thenReturn(List.of(dominio));

        mockMvc.perform(get("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].placa").value("ABC1D23"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 200 OK ao atualizar veículo existente")
    void deveAtualizarVeiculoComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        VeiculoRequest request = new VeiculoRequest(
                "ABC1D23", "Toyota", "Corolla Cross", 12000, Short.valueOf("2023"), "Preto", clienteId
        );
        Veiculo dominioAtualizado = new Veiculo(
                id, "ABC1D23", "Toyota", "Corolla Cross", 12000, Short.valueOf("2023"), "Preto", clienteId
        );

        when(atualizarVeiculoUseCase.atualizar(eq(id), any(Veiculo.class))).thenReturn(dominioAtualizado);

        mockMvc.perform(put("/veiculos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla Cross"))
                .andExpect(jsonPath("$.cor").value("Preto"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 204 No Content ao deletar veículo com sucesso")
    void deveDeletarVeiculoComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(deletarVeiculoUseCase).deletar(id);

        mockMvc.perform(delete("/veiculos/{id}", id))
                .andExpect(status().isNoContent());

        verify(deletarVeiculoUseCase, times(1)).deletar(id);
    }
}