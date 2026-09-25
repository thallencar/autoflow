package br.com.autoflow.application.usecase;

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
    private EnderecoValidator enderecoValidator;

    @InjectMocks
    private EnderecoUseCaseImpl service;

    private Endereco criarDominioExemplo() {
        return new Endereco(
                UUID.randomUUID(),
                "Rua Principal",
                "RS",
                "Apto 101",
                "Centro",
                "Novo Hamburgo",
                100,
                "93520000"
        );
    }

    @Nested
    @DisplayName("Testes de Criação (criar)")
    class CriarTests {

        @Test
        @DisplayName("Deve criar endereço com sucesso quando os dados forem válidos")
        void deveCriarEnderecoComSucesso() {
            // Arrange
            Endereco endereco = criarDominioExemplo();

            doNothing().when(enderecoValidator).validarUf(endereco);
            when(repository.save(endereco)).thenReturn(endereco);

            // Act
            Endereco resultado = service.criar(endereco);

            // Assert
            assertNotNull(resultado);
            assertEquals(endereco, resultado);

            verify(enderecoValidator).validarUf(endereco);
            verify(repository).save(endereco);
        }

        @Test
        @DisplayName("Não deve salvar endereço se a validação da UF falhar")
        void naoDeveCriarSeValidacaoFalhar() {
            // Arrange
            Endereco endereco = criarDominioExemplo();
            doThrow(new IllegalArgumentException("UF inválida")).when(enderecoValidator).validarUf(endereco);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.criar(endereco));

            verify(enderecoValidator).validarUf(endereco);
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
            Endereco e1 = criarDominioExemplo();
            Endereco e2 = criarDominioExemplo();

            when(repository.findAll()).thenReturn(List.of(e1, e2));

            // Act
            List<Endereco> resultado = service.listar();

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
            List<Endereco> resultado = service.listar();

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
            Endereco endereco = criarDominioExemplo();

            when(repository.findById(id)).thenReturn(Optional.of(endereco));

            // Act
            Endereco resultado = service.buscar(id);

            // Assert
            assertNotNull(resultado);
            assertEquals(endereco, resultado);
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
            Endereco enderecoExistente = mock(Endereco.class);
            Endereco enderecoParam = criarDominioExemplo();

            when(repository.findById(id)).thenReturn(Optional.of(enderecoExistente));
            doNothing().when(enderecoValidator).validarUf(enderecoParam);
            when(repository.save(enderecoExistente)).thenReturn(enderecoExistente);

            // Act
            Endereco resultado = service.atualizar(id, enderecoParam);

            // Assert
            assertNotNull(resultado);

            verify(repository).findById(id);
            verify(enderecoValidator).validarUf(enderecoParam);
            verify(enderecoExistente).atualizar(any(), any(), any(), any(), any(), any(), any());
            verify(repository).save(enderecoExistente);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar atualizar ID inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            // Arrange
            UUID id = UUID.randomUUID();
            Endereco enderecoParam = criarDominioExemplo();

            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(id, enderecoParam));

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
            Endereco endereco = criarDominioExemplo();

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