package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import br.com.autoflow.adapters.inbound.mapper.ClienteMapper;
import br.com.autoflow.application.usecase.ClienteUseCaseImpl;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClienteUseCaseImpl clienteService;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteController clienteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController).build();
    }

    private String criarJsonRequestValido() {
        return """
                {
                  "documento": "12345678909",
                  "nome": "Ana Silva",
                  "telefone": "51999998888",
                  "email": "ana@email.com",
                  "genero": "FEMININO",
                  "dataNascimento": "1995-05-15",
                  "endereco": {
                    "logradouro": "Rua A",
                    "uf": "RS",
                    "cidade": "Porto Alegre",
                    "bairro": "Centro",
                    "numero": "123",
                    "cep": 93500000,
                    "complemento": "casa"
                  }
                }
                """;
    }

    private String criarJsonUpdateRequestValido() {
        return """
                {
                  "nome": "Ana Silva Atualizada",
                  "telefone": "51988887777",
                  "email": "ana.nova@email.com",
                  "genero": "FEMININO",
                  "endereco": {
                    "logradouro": "Rua B",
                    "uf": "RS",
                    "cidade": "Porto Alegre",
                    "bairro": "Centro",
                    "numero": "456",
                    "cep": 93500000,
                    "complemento": "apto"
                  }
                }
                """;
    }

    private Cliente criarClienteDomainExemplo(UUID id) {
        Endereco endereco = new Endereco(null, "Rua A", "RS", "Casa", "Centro", "Porto Alegre", 123, "93500000");
        return new Cliente(
                id,
                "Ana Silva",
                "12345678909",
                "ana@email.com",
                LocalDate.of(1995, 5, 15),
                "51999998888",
                Genero.FEMININO,
                endereco
        );
    }

    private Cliente criarClienteDomainAtualizadoExemplo(UUID id) {
        Endereco endereco = new Endereco(null, "Rua B", "RS", "apto", "Centro", "Porto Alegre", 456, "93500000");
        return new Cliente(
                id,
                "Ana Silva Atualizada",
                "12345678909",
                "ana.nova@email.com",
                LocalDate.of(1995, 5, 15),
                "51988887777",
                Genero.FEMININO,
                endereco
        );
    }

    private ClienteResponse criarResponseExemplo(UUID id) {
        EnderecoResponse enderecoResponse = new EnderecoResponse(
                UUID.randomUUID(), "Rua A", 123, "Casa", "Centro", "Porto Alegre", "RS", "93500000"
        );
        return new ClienteResponse(
                id,
                "Ana Silva",
                "12345678909",
                "ana@email.com",
                LocalDate.of(1995, 5, 15),
                "51999998888",
                Genero.FEMININO,
                enderecoResponse
        );
    }

    private ClienteResponse criarResponseAtualizadoExemplo(UUID id) {
        EnderecoResponse enderecoResponse = new EnderecoResponse(
                UUID.randomUUID(), "Rua B", 456, "apto", "Centro", "Porto Alegre", "RS", "93500000"
        );
        return new ClienteResponse(
                id,
                "Ana Silva Atualizada",
                "12345678909",
                "ana.nova@email.com",
                LocalDate.of(1995, 5, 15),
                "51988887777",
                Genero.FEMININO,
                enderecoResponse
        );
    }

    @Nested
    @DisplayName("POST /clientes")
    class CriarTests {

        @Test
        @DisplayName("Deve retornar HTTP 201 Created e o cliente criado")
        void deveCriarClienteComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Cliente clienteDomain = criarClienteDomainExemplo(id);
            ClienteResponse response = criarResponseExemplo(id);

            when(clienteMapper.toEnderecoDomain(any())).thenReturn(clienteDomain.getEndereco());
            when(clienteMapper.toDomain(any(ClienteRequest.class), any())).thenReturn(clienteDomain);
            when(clienteService.criar(any(Cliente.class))).thenReturn(clienteDomain);
            when(clienteMapper.toResponse(clienteDomain)).thenReturn(response);

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonRequestValido()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.nome").value("Ana Silva"))
                    .andExpect(jsonPath("$.documento").value("12345678909"))
                    .andExpect(jsonPath("$.email").value("ana@email.com"));

            verify(clienteService).criar(any(Cliente.class));
        }
    }

    @Nested
    @DisplayName("GET /clientes")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista de clientes")
        void deveListarClientesComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Cliente clienteDomain = criarClienteDomainExemplo(id);
            ClienteResponse response = criarResponseExemplo(id);

            when(clienteService.listar()).thenReturn(List.of(clienteDomain));
            when(clienteMapper.toResponse(clienteDomain)).thenReturn(response);

            mockMvc.perform(get("/clientes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].nome").value("Ana Silva"));

            verify(clienteService).listar();
        }

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com lista vazia")
        void deveRetornarListaVazia() throws Exception {
            when(clienteService.listar()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/clientes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(clienteService).listar();
        }
    }

    @Nested
    @DisplayName("GET /clientes/{id}")
    class BuscarPorIdTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK quando encontrar o cliente por ID")
        void deveBuscarPorIdComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Cliente clienteDomain = criarClienteDomainExemplo(id);
            ClienteResponse response = criarResponseExemplo(id);

            when(clienteService.buscarPorId(id)).thenReturn(clienteDomain);
            when(clienteMapper.toResponse(clienteDomain)).thenReturn(response);

            mockMvc.perform(get("/clientes/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.nome").value("Ana Silva"));

            verify(clienteService).buscarPorId(id);
        }
    }

    @Nested
    @DisplayName("GET /clientes/documento/{documento}")
    class BuscarPorDocumentoTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK quando encontrar o cliente por documento")
        void deveBuscarPorDocumentoComSucesso() throws Exception {
            String documento = "12345678909";
            UUID id = UUID.randomUUID();
            Cliente clienteDomain = criarClienteDomainExemplo(id);
            ClienteResponse response = criarResponseExemplo(id);

            when(clienteService.buscarPorDocumento(documento)).thenReturn(clienteDomain);
            when(clienteMapper.toResponse(clienteDomain)).thenReturn(response);

            mockMvc.perform(get("/clientes/documento/{documento}", documento)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.documento").value(documento))
                    .andExpect(jsonPath("$.nome").value("Ana Silva"));

            verify(clienteService).buscarPorDocumento(documento);
        }
    }

    @Nested
    @DisplayName("PUT /clientes/{id}")
    class AtualizarTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK e o cliente atualizado")
        void deveAtualizarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            Cliente clienteDomainAtualizado = criarClienteDomainAtualizadoExemplo(id);
            ClienteResponse responseAtualizado = criarResponseAtualizadoExemplo(id);

            when(clienteMapper.toEnderecoDomain(any())).thenReturn(clienteDomainAtualizado.getEndereco());
            when(clienteMapper.toDomain(any(ClienteUpdateRequest.class), any())).thenReturn(clienteDomainAtualizado);
            when(clienteService.atualizar(eq(id), any(Cliente.class))).thenReturn(clienteDomainAtualizado);
            when(clienteMapper.toResponse(clienteDomainAtualizado)).thenReturn(responseAtualizado);

            mockMvc.perform(put("/clientes/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonUpdateRequestValido()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.nome").value("Ana Silva Atualizada"))
                    .andExpect(jsonPath("$.email").value("ana.nova@email.com"));

            verify(clienteService).atualizar(eq(id), any(Cliente.class));
        }
    }

    @Nested
    @DisplayName("DELETE /clientes/{id}")
    class DeletarTests {

        @Test
        @DisplayName("Deve retornar HTTP 204 No Content ao deletar com sucesso")
        void deveDeletarComSucesso() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(clienteService).deletar(id);

            mockMvc.perform(delete("/clientes/{id}", id))
                    .andExpect(status().isNoContent());

            verify(clienteService).deletar(id);
        }
    }
}