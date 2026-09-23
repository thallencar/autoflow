package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import br.com.autoflow.adapters.inbound.mapper.EnderecoMapper;
import br.com.autoflow.application.validator.EnderecoValidator;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepositoryPort repository;

    @Mock
    private EnderecoMapper enderecoMapper;

    @Mock
    private EnderecoValidator enderecoValidator;

    @InjectMocks
    private EnderecoUseCase service;

    private EnderecoRequest criarRequestExemplo() {
        return new EnderecoRequest(
                "Rua Principal", "RS", "Novo Hamburgo", "Centro", "100", 93520000, "Apto 101"
        );
    }

    @Nested
    @DisplayName("Testes de Criação (criar)")
    class CriarTests {

        @Test
        @DisplayName("Deve criar endereço com sucesso quando os dados forem válidos")
        void deveCriarEnderecoComSucesso() {
            // Arrange
            EnderecoRequest request = criarRequestExemplo();
            Endereco enderecoMock = mock(Endereco.class);
            EnderecoResponse responseEsperada = mock(EnderecoResponse.class);

            doNothing().when(enderecoValidator).validarUf(request);
            when(enderecoMapper.toDomain(request)).thenReturn(enderecoMock);
            when(repository.save(enderecoMock)).thenReturn(enderecoMock);
            when(enderecoMapper.toResponse(enderecoMock)).thenReturn(responseEsperada);

            // Act
            EnderecoResponse response = service.criar(request);

            // Assert
            assertNotNull(response);
            assertEquals(responseEsperada, response);

            verify(enderecoValidator).validarUf(request);
            verify(enderecoMapper).toDomain(request);
            verify(repository).save(enderecoMock);
            verify(enderecoMapper).toResponse(enderecoMock);
        }

        @Test
        @DisplayName("Não deve salvar endereço se a validação da UF falhar")
        void naoDeveCriarSeValidacaoFalhar() {
            // Arrange
            EnderecoRequest request = criarRequestExemplo();
            doThrow(new IllegalArgumentException("UF inválida")).when(enderecoValidator).validarUf(request);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.criar(request));

            verify(enderecoValidator).validarUf(request);
            verify(enderecoMapper, never()).toDomain(any());
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Listagem (listar)")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar lista de endereços quando houver registros")
        void deveListarEnderecosComSucesso() {
            // Arrange
            Endereco e1 = mock(Endereco.class);
            Endereco e2 = mock(Endereco.class);
            EnderecoResponse responseMock = mock(EnderecoResponse.class);

            when(repository.findAll()).thenReturn(List.of(e1, e2));
            when(enderecoMapper.toResponse(any(Endereco.class))).thenReturn(responseMock);

            // Act
            List<EnderecoResponse> resultado = service.listar();

            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(repository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver endereços cadastrados")
        void deveRetornarListaVaziaQuandoNaoHouverRegistros() {
            // Arrange
            when(repository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<EnderecoResponse> resultado = service.listar();

            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(repository).findAll();
        }
    }

    @Nested
    @DisplayName("Testes de Busca por ID (buscar)")
    class BuscarTests {

        @Test
        @DisplayName("Deve retornar endereço quando ID for encontrado")
        void deveBuscarPorIdComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Endereco endereco = mock(Endereco.class);
            EnderecoResponse responseEsperada = mock(EnderecoResponse.class);

            when(repository.findById(id)).thenReturn(Optional.of(endereco));
            when(enderecoMapper.toResponse(endereco)).thenReturn(responseEsperada);

            // Act
            EnderecoResponse response = service.buscar(id);

            // Assert
            assertNotNull(response);
            assertEquals(responseEsperada, response);
            verify(repository).findById(id);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando ID não existir")
        void deveLancarExcecaoQuandoIdNaoEncontrado() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            EntidadeNaoEncontradaException ex = assertThrows(
                    EntidadeNaoEncontradaException.class, () -> service.buscar(id)
            );
            assertNotNull(ex);
            verify(repository).findById(id);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização (atualizar)")
    class AtualizarTests {

        @Test
        @DisplayName("Deve atualizar endereço com sucesso quando ID existir")
        void deveAtualizarComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            EnderecoRequest request = criarRequestExemplo();
            Endereco endereco = mock(Endereco.class);
            EnderecoResponse responseEsperada = mock(EnderecoResponse.class);

            when(repository.findById(id)).thenReturn(Optional.of(endereco));
            when(repository.save(endereco)).thenReturn(endereco);
            when(enderecoMapper.toResponse(endereco)).thenReturn(responseEsperada);

            // Act
            EnderecoResponse response = service.atualizar(id, request);

            // Assert
            assertNotNull(response);
            assertEquals(responseEsperada, response);

            verify(repository).findById(id);
            verify(endereco).atualizar(any(), any(), any(), any(), any(), any(), any());
            verify(repository).save(endereco);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar atualizar ID inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            // Arrange
            UUID id = UUID.randomUUID();
            EnderecoRequest request = criarRequestExemplo();

            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(id, request));

            verify(repository).findById(id);
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Exclusão (deletar)")
    class DeletarTests {

        @Test
        @DisplayName("Deve deletar endereço quando ID for encontrado")
        void deveDeletarComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Endereco endereco = mock(Endereco.class);

            when(repository.findById(id)).thenReturn(Optional.of(endereco));

            // Act
            service.deletar(id);

            // Assert
            verify(repository).findById(id);
            verify(repository).delete(endereco);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar deletar ID inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(id));

            verify(repository).findById(id);
            verify(repository, never()).delete(any());
        }
    }
}