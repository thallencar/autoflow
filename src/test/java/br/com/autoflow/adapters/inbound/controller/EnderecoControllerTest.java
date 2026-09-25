package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import br.com.autoflow.adapters.inbound.mapper.EnderecoMapper;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.ports.inbound.endereco.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
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
    private CriarEnderecoUseCase criarEnderecoUseCase;

    @Mock
    private ListarEnderecosUseCase listarEnderecosUseCase;

    @Mock
    private BuscarEnderecoPorIdUseCase buscarEnderecoPorIdUseCase;

    @Mock
    private AtualizarEnderecoUseCase atualizarEnderecoUseCase;

    @Mock
    private DeletarEnderecoUseCase deletarEnderecoUseCase;

    // Usamos o Mapper real (ou um Spy) para que a conversão DTO <-> Domínio ocorra de verdade durante os testes do MockMvc
    @Spy
    private EnderecoMapper enderecoMapper = Mappers.getMapper(EnderecoMapper.class);

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
                  "cep": "93520000"
                }
                """;
    }

    private Endereco criarDominioExemplo(UUID id) {
        return  new Endereco(
                id,
                "Rua Principal",
                "RS",
                "Apto 101",
                "Centro",
                "Novo Hamburgo",
                123,
                "93520000"
        );
    }

    @Nested
    @DisplayName("POST /enderecos")
    class CriarTests {

        @Test
        @DisplayName("Deve retornar HTTP 201 Created e o endereço criado")
        void deveCriarEnderecoComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Endereco dominio = criarDominioExemplo(id);

            when(criarEnderecoUseCase.criar(any(Endereco.class))).thenReturn(dominio);

            mockMvc.perform(post("/enderecos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$.logradouro").value("Rua Principal"))
                    .andExpect(jsonPath("$.cidade").value("Novo Hamburgo"))
                    .andExpect(jsonPath("$.uf").value("RS"));

            verify(criarEnderecoUseCase).criar(any(Endereco.class));
        }
    }

    @Nested
    @DisplayName("GET /enderecos")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista de endereços")
        void deveListarEnderecosComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Endereco dominio = criarDominioExemplo(id);

            when(listarEnderecosUseCase.listar()).thenReturn(List.of(dominio));

            mockMvc.perform(get("/enderecos")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$[0].logradouro").value("Rua Principal"));

            verify(listarEnderecosUseCase).listar();
        }

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista vazia")
        void deveRetornarListaVazia() throws Exception {
            when(listarEnderecosUseCase.listar()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/enderecos")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(listarEnderecosUseCase).listar();
        }
    }

    @Nested
    @DisplayName("GET /enderecos/{id}")
    class BuscarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK quando encontrar o endereço por ID")
        void deveBuscarPorIdComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Endereco dominio = criarDominioExemplo(id);

            when(buscarEnderecoPorIdUseCase.buscar(id)).thenReturn(dominio);

            mockMvc.perform(get("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$.logradouro").value("Rua Principal"));

            verify(buscarEnderecoPorIdUseCase).buscar(id);
        }

        @Test
        @DisplayName("Deve propagar exceção quando endereço não for encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(buscarEnderecoPorIdUseCase.buscar(id)).thenThrow(new EntidadeNaoEncontradaException("Endereço", id));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(get("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
            );

            verify(buscarEnderecoPorIdUseCase).buscar(id);
        }
    }

    @Nested
    @DisplayName("PUT /enderecos/{id}")
    class AtualizarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK e o endereço atualizado")
        void deveAtualizarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Endereco dominio = criarDominioExemplo(id);

            when(atualizarEnderecoUseCase.atualizar(eq(id), any(Endereco.class))).thenReturn(dominio);

            mockMvc.perform(put("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEndereco").value(id.toString()))
                    .andExpect(jsonPath("$.logradouro").value("Rua Principal"));

            verify(atualizarEnderecoUseCase).atualizar(eq(id), any(Endereco.class));
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar atualizar ID inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            UUID id = UUID.randomUUID();
            when(atualizarEnderecoUseCase.atualizar(eq(id), any(Endereco.class)))
                    .thenThrow(new EntidadeNaoEncontradaException("Endereço", id));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(put("/enderecos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
            );

            verify(atualizarEnderecoUseCase).atualizar(eq(id), any(Endereco.class));
        }
    }

    @Nested
    @DisplayName("DELETE /enderecos/{id}")
    class DeletarTests {

        @Test
        @DisplayName("Deve retornar HTTP 204 No Content ao deletar com sucesso")
        void deveDeletarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(deletarEnderecoUseCase).deletar(id);

            mockMvc.perform(delete("/enderecos/{id}", id))
                    .andExpect(status().isNoContent());

            verify(deletarEnderecoUseCase).deletar(id);
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar deletar ID inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            UUID id = UUID.randomUUID();
            doThrow(new EntidadeNaoEncontradaException("Endereço", id)).when(deletarEnderecoUseCase).deletar(id);

            assertThrows(Exception.class, () ->
                    mockMvc.perform(delete("/enderecos/{id}", id))
            );

            verify(deletarEnderecoUseCase).deletar(id);
        }
    }
}