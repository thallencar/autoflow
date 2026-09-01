package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.application.dto.VeiculoResponse;
import br.com.autoflow.application.service.VeiculoService;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    private VeiculoService veiculoService;

    @InjectMocks
    private VeiculoController veiculoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(veiculoController)
                .setControllerAdvice(new br.com.autoflow.exception.GlobalExceptionHandler()) // Adicione seu ExceptionHandler aqui se tiver!
                .build();
    }

    @Test
    @DisplayName("Deve retornar HTTP 201 Created ao criar veículo válido")
    void deveCriarVeiculoComSucesso() throws Exception {
        UUID clienteId = UUID.randomUUID();
        VeiculoRequest request = new VeiculoRequest(
                "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2022"), "Prata", clienteId
        );
        VeiculoResponse response = new VeiculoResponse(
                UUID.randomUUID(), "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2022"), "Prata", clienteId
        );

        when(veiculoService.criar(any())).thenReturn(response);

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
        when(veiculoService.buscarPorId(idInexistente))
                .thenThrow(new EntidadeNaoEncontradaException("Veículo", idInexistente));

        mockMvc.perform(get("/veiculos/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar HTTP 200 OK ao buscar veículo por ID existente")
    void deveBuscarVeiculoPorIdComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        VeiculoResponse response = new VeiculoResponse(
                id, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2022"), "Prata", clienteId
        );

        when(veiculoService.buscarPorId(id)).thenReturn(response);

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
        VeiculoResponse response = new VeiculoResponse(
                id, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2022"), "Prata", clienteId
        );

        when(veiculoService.listar()).thenReturn(List.of(response));

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
        VeiculoResponse response = new VeiculoResponse(
                id, "ABC1D23", "Toyota", "Corolla Cross", 12000, Short.valueOf("2023"), "Preto", clienteId
        );

        when(veiculoService.atualizar(eq(id), any())).thenReturn(response);

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
        doNothing().when(veiculoService).deletar(id);

        mockMvc.perform(delete("/veiculos/{id}", id))
                .andExpect(status().isNoContent());

        verify(veiculoService, times(1)).deletar(id);
    }
}