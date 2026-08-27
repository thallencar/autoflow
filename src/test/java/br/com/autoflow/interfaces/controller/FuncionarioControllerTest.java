package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.EnderecoResponse;
import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.application.service.FuncionarioService;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import jakarta.servlet.ServletException;
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

import java.time.LocalDate;
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
class FuncionarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FuncionarioService service;

    @InjectMocks
    private FuncionarioController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private String criarJsonRequestValido() {
        return """
                {
                  "cpf": "62157435000",
                  "nome": "Carlos Silva",
                  "telefone": "51999999999",
                  "email": "carlos@gmail.com",
                  "genero": "MASCULINO",
                  "dataNascimento": "2000-09-12",
                  "cargo": "GERENTE",
                  "endereco": {
                    "logradouro": "Rua A",
                    "uf": "RS",
                    "cidade": "Cidade C",
                    "bairro": "Bairro C",
                    "numero": "123",
                    "cep": 93500000,
                    "complemento": "casa"
                  }
                }
                """;
    }

    private FuncionarioResponse criarResponseExemplo(UUID id) {
        EnderecoResponse enderecoResponse = new EnderecoResponse(
                UUID.randomUUID(), "Rua A", 123, "Casa", "Bairro C", "Cidade", "RS", "93500000"
        );
        return new FuncionarioResponse(
                id, "62157435000", "Carlos Silva", "51999999999", "carlos@gmail.com",
                Genero.MASCULINO, LocalDate.of(2000, 9, 12), Cargo.GERENTE, false, enderecoResponse
        );
    }

    @Nested
    @DisplayName("POST /funcionarios")
    class CriarTests {

        @Test
        @DisplayName("Deve retornar HTTP 201 Created e o funcionário criado")
        void deveCriarFuncionarioComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            FuncionarioResponse response = criarResponseExemplo(id);

            when(service.criar(any(FuncionarioRequest.class))).thenReturn(response);

            mockMvc.perform(post("/funcionarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.nome").value("Carlos Silva"))
                    .andExpect(jsonPath("$.cpf").value("62157435000"))
                    .andExpect(jsonPath("$.email").value("carlos@gmail.com"));

            verify(service).criar(any(FuncionarioRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /funcionarios")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista de funcionários")
        void deveListarFuncionariosComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            FuncionarioResponse response = criarResponseExemplo(id);

            when(service.listar()).thenReturn(List.of(response));

            mockMvc.perform(get("/funcionarios")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].nome").value("Carlos Silva"));

            verify(service).listar();
        }

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista vazia")
        void deveRetornarListaVazia() throws Exception {
            when(service.listar()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/funcionarios")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(service).listar();
        }
    }

    @Nested
    @DisplayName("GET /funcionarios/{id}")
    class BuscarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK quando encontrar o funcionário por ID")
        void deveBuscarPorIdComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            FuncionarioResponse response = criarResponseExemplo(id);

            when(service.buscar(id)).thenReturn(response);

            mockMvc.perform(get("/funcionarios/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.nome").value("Carlos Silva"));

            verify(service).buscar(id);
        }

        @Test
        @DisplayName("Deve propagar exceção quando funcionário não for encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(service.buscar(id)).thenThrow(new EntidadeNaoEncontradaException("Funcionário", id));

            ServletException exception = assertThrows(ServletException.class, () ->
                    mockMvc.perform(get("/funcionarios/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
            );

            assertTrue(exception.getCause() instanceof EntidadeNaoEncontradaException);
            assertEquals("Funcionário com ID " + id + " nao encontrado.", exception.getCause().getMessage());
            verify(service).buscar(id);
        }
    }

    @Nested
    @DisplayName("PUT /funcionarios/{id}")
    class AtualizarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK e o funcionário atualizado")
        void deveAtualizarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            FuncionarioResponse response = criarResponseExemplo(id);

            when(service.atualizar(eq(id), any(FuncionarioRequest.class))).thenReturn(response);

            mockMvc.perform(put("/funcionarios/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.nome").value("Carlos Silva"));

            verify(service).atualizar(eq(id), any(FuncionarioRequest.class));
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar atualizar ID inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            UUID id = UUID.randomUUID();
            when(service.atualizar(eq(id), any(FuncionarioRequest.class)))
                    .thenThrow(new EntidadeNaoEncontradaException("Funcionário", id));

            ServletException exception = assertThrows(ServletException.class, () ->
                    mockMvc.perform(put("/funcionarios/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
            );

            assertTrue(exception.getCause() instanceof EntidadeNaoEncontradaException);
            assertEquals("Funcionário com ID " + id + " nao encontrado.", exception.getCause().getMessage());
            verify(service).atualizar(eq(id), any(FuncionarioRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /funcionarios/{id}")
    class DeletarTests {

        @Test
        @DisplayName("Deve retornar HTTP 204 No Content ao deletar com sucesso")
        void deveDeletarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(service).deletar(id);

            mockMvc.perform(delete("/funcionarios/{id}", id))
                    .andExpect(status().isNoContent());

            verify(service).deletar(id);
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar deletar ID inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            UUID id = UUID.randomUUID();
            doThrow(new EntidadeNaoEncontradaException("Funcionário", id)).when(service).deletar(id);

            ServletException exception = assertThrows(ServletException.class, () ->
                    mockMvc.perform(delete("/funcionarios/{id}", id))
            );

            assertTrue(exception.getCause() instanceof EntidadeNaoEncontradaException);
            assertEquals("Funcionário com ID " + id + " nao encontrado.", exception.getCause().getMessage());
            verify(service).deletar(id);
        }
    }
}