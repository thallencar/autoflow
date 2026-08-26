package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.EnderecoResponse;
import br.com.autoflow.application.service.EnderecoService;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnderecoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnderecoService enderecoService;

    @InjectMocks
    private EnderecoController enderecoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(enderecoController).build();
    }

    private String criarJsonRequestValido() {
        return """
                {
                  "logradouro": "Rua Principal",
                  "numero": 100,
                  "complemento": "Apto 101",
                  "bairro": "Centro",
                  "cidade": "Novo Hamburgo",
                  "uf": "RS",
                  "cep": 93520000
                }
                """;
    }

    private EnderecoResponse criarResponseExemplo(UUID id) {
        return new EnderecoResponse(
                id, "Rua Principal", 100, "Apto 101", "Centro", "Novo Hamburgo", "RS", "93520000"
        );
    }

    @Nested
    @DisplayName("POST /enderecos")
    class CriarTests {

        @Test
        @DisplayName("Deve retornar HTTP 201 Created e o endereço criado")
        void deveCriarEnderecoComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            EnderecoResponse response = criarResponseExemplo(id);

            when(enderecoService.criar(any(EnderecoRequest.class))).thenReturn(response);

            mockMvc.perform(post("/enderecos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$.logradouro").value("Rua Principal"))
                    .andExpect(jsonPath("$.cidade").value("Novo Hamburgo"))
                    .andExpect(jsonPath("$.uf").value("RS"));

            verify(enderecoService).criar(any(EnderecoRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /enderecos")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista de endereços")
        void deveListarEnderecosComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            EnderecoResponse response = criarResponseExemplo(id);

            when(enderecoService.listar()).thenReturn(List.of(response));

            mockMvc.perform(get("/enderecos")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$[0].logradouro").value("Rua Principal"));

            verify(enderecoService).listar();
        }

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista vazia")
        void deveRetornarListaVazia() throws Exception {
            when(enderecoService.listar()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/enderecos")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(enderecoService).listar();
        }
    }

    @Nested
    @DisplayName("GET /enderecos/{id}")
    class BuscarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK quando encontrar o endereço por ID")
        void deveBuscarPorIdComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            EnderecoResponse response = criarResponseExemplo(id);

            when(enderecoService.buscar(id)).thenReturn(response);

            mockMvc.perform(get("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$.logradouro").value("Rua Principal"));

            verify(enderecoService).buscar(id);
        }

        @Test
        @DisplayName("Deve propagar exceção quando endereço não for encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(enderecoService.buscar(id)).thenThrow(new EntidadeNaoEncontradaException("Endereço", id));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(get("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
            );

            verify(enderecoService).buscar(id);
        }
    }

    @Nested
    @DisplayName("PUT /enderecos/{id}")
    class AtualizarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK e o endereço atualizado")
        void deveAtualizarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            EnderecoResponse response = criarResponseExemplo(id);

            when(enderecoService.atualizar(eq(id), any(EnderecoRequest.class))).thenReturn(response);

            mockMvc.perform(put("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$.logradouro").value("Rua Principal"));

            verify(enderecoService).atualizar(eq(id), any(EnderecoRequest.class));
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar atualizar ID inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            UUID id = UUID.randomUUID();
            when(enderecoService.atualizar(eq(id), any(EnderecoRequest.class)))
                    .thenThrow(new EntidadeNaoEncontradaException("Endereço", id));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(put("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
            );

            verify(enderecoService).atualizar(eq(id), any(EnderecoRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /enderecos/{id}")
    class DeletarTests {

        @Test
        @DisplayName("Deve retornar HTTP 204 No Content ao deletar com sucesso")
        void deveDeletarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(enderecoService).deletar(id);

            mockMvc.perform(delete("/enderecos/{id}", id))
                    .andExpect(status().isNoContent());

            verify(enderecoService).deletar(id);
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar deletar ID inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            UUID id = UUID.randomUUID();
            doThrow(new EntidadeNaoEncontradaException("Endereço", id)).when(enderecoService).deletar(id);

            assertThrows(Exception.class, () ->
                    mockMvc.perform(delete("/enderecos/{id}", id))
            );

            verify(enderecoService).deletar(id);
        }
    }
}