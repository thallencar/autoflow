package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.domain.repository.EnderecoRepository;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import br.com.autoflow.domain.repository.UsuarioRepository;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.infrastructure.mapper.EnderecoMapper;
import br.com.autoflow.infrastructure.mapper.FuncionarioMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository repository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private FuncionarioMapper funcionarioMapper;

    @Mock
    private EnderecoMapper enderecoMapper;

    @Mock
    private FuncionarioValidator funcionarioValidator;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private FuncionarioService service;

    private FuncionarioRequest criarRequestExemplo(Cargo cargo) {
        EnderecoRequest enderecoRequest = new EnderecoRequest("Rua A", "RS", "Cidade C", "Bairro C", "123", 93500000, "casa");
        return new FuncionarioRequest(
                "12345678901", "Carlos Silva", "51999999999", "carlos@gmail.com",
                Genero.MASCULINO, LocalDate.of(2000, 9, 12), cargo, enderecoRequest
        );
    }

    @Nested
    @DisplayName("Testes de Criação")
    class CriarTests {

        @ParameterizedTest
        @CsvSource({
                "GERENTE, ADMIN",
                "RECEPCIONISTA, ADMIN",
                "MECANICO, MECANICO",
                "AUXILIAR_MECANICO, MECANICO"
        })
        @DisplayName("Deve criar funcionário e vincular usuário com o perfil correto para cada cargo")
        void deveCriarFuncionarioEUsuarioComSucesso(Cargo cargo, Perfil perfilEsperado) {
            // Arrange
            FuncionarioRequest request = criarRequestExemplo(cargo);

            Funcionario funcionarioMock = mock(Funcionario.class);
            when(funcionarioMock.getCargo()).thenReturn(cargo);

            FuncionarioResponse responseEsperada = mock(FuncionarioResponse.class);

            doNothing().when(funcionarioValidator).validarParaCriar(request);
            when(funcionarioMapper.toEntity(request)).thenReturn(funcionarioMock);
            when(repository.save(funcionarioMock)).thenReturn(funcionarioMock);
            when(funcionarioMapper.toResponse(funcionarioMock)).thenReturn(responseEsperada);

            // Act
            FuncionarioResponse response = service.criar(request);

            // Assert
            assertNotNull(response);
            assertEquals(responseEsperada, response);

            // Verificações dos serviços chamados
            verify(funcionarioValidator).validarParaCriar(request);
            verify(funcionarioMapper).toEntity(request);
            verify(repository).save(funcionarioMock);

            // Captura do usuário salvo para garantir que foi associado
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(usuarioCaptor.capture());
            assertNotNull(usuarioCaptor.getValue());
        }

        @Test
        @DisplayName("Não deve salvar entidades se a validação lançar exceção")
        void naoDeveCriarSeValidacaoFalhar() {
            // Arrange
            FuncionarioRequest request = criarRequestExemplo(Cargo.GERENTE);
            doThrow(new DadosJaCadastradosException("CPF já cadastrado"))
                    .when(funcionarioValidator).validarParaCriar(request);

            // Act & Assert
            assertThrows(DadosJaCadastradosException.class, () -> service.criar(request));

            verify(enderecoRepository, never()).save(any());
            verify(repository, never()).save(any());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Listagem (listar)")
    class ListarTests {

        @Test
        @DisplayName("Deve retornar lista de funcionários quando houver registros")
        void deveListarFuncionariosComSucesso() {
            // Arrange
            Funcionario f1 = mock(Funcionario.class);
            Funcionario f2 = mock(Funcionario.class);
            FuncionarioResponse responseMock = mock(FuncionarioResponse.class);

            when(repository.findAll()).thenReturn(List.of(f1, f2));
            when(funcionarioMapper.toResponse(any(Funcionario.class))).thenReturn(responseMock);

            // Act
            List<FuncionarioResponse> resultado = service.listar();

            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(repository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver funcionários cadastrados")
        void deveRetornarListaVaziaQuandoNaoHouverRegistros() {
            // Arrange
            when(repository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<FuncionarioResponse> resultado = service.listar();

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
        @DisplayName("Deve retornar funcionário quando ID for encontrado")
        void deveBuscarPorIdComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Funcionario funcionario = mock(Funcionario.class);
            FuncionarioResponse responseEsperada = mock(FuncionarioResponse.class);

            when(repository.findById(id)).thenReturn(Optional.of(funcionario));
            when(funcionarioMapper.toResponse(funcionario)).thenReturn(responseEsperada);

            // Act
            FuncionarioResponse response = service.buscar(id);

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
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscar(id));
            verify(repository).findById(id);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização (atualizar)")
    class AtualizarTests {

        @ParameterizedTest
        @CsvSource({
                "GERENTE, ADMIN",
                "RECEPCIONISTA, ADMIN",
                "MECANICO, MECANICO",
                "AUXILIAR_MECANICO, MECANICO"
        })
        @DisplayName("Deve atualizar funcionário e sincronizar perfil correto do usuário vinculado")
        void deveAtualizarFuncionarioEUsuarioComPerfilCorreto(Cargo cargo, Perfil perfilEsperado) {
            // Arrange
            UUID id = UUID.randomUUID();
            FuncionarioRequest request = criarRequestExemplo(cargo);

            Funcionario funcionario = mock(Funcionario.class);
            when(funcionario.getEmail()).thenReturn("carlos@gmail.com");
            when(funcionario.getCargo()).thenReturn(cargo);

            Usuario usuarioMock = mock(Usuario.class);
            FuncionarioResponse responseEsperada = mock(FuncionarioResponse.class);

            doNothing().when(funcionarioValidator).validarParaAtualizar(id, request);
            when(repository.findById(id)).thenReturn(Optional.of(funcionario));
            when(usuarioRepository.findByFuncionario(funcionario)).thenReturn(Optional.of(usuarioMock));
            when(funcionarioMapper.toResponse(funcionario)).thenReturn(responseEsperada);

            // Act
            FuncionarioResponse response = service.atualizar(id, request);

            // Assert
            assertNotNull(response);
            assertEquals(responseEsperada, response);

            verify(funcionarioValidator).validarParaAtualizar(id, request);
            verify(funcionarioMapper).updateEntityFromDto(request, funcionario);
            verify(usuarioMock).atualizarDadosAcesso("carlos@gmail.com", perfilEsperado);
        }

        @Test
        @DisplayName("Deve atualizar funcionário com sucesso mesmo quando não houver usuário vinculado")
        void deveAtualizarApenasFuncionarioQuandoNaoHouverUsuario() {
            // Arrange
            UUID id = UUID.randomUUID();
            FuncionarioRequest request = criarRequestExemplo(Cargo.MECANICO);
            Funcionario funcionario = mock(Funcionario.class);
            FuncionarioResponse responseEsperada = mock(FuncionarioResponse.class);

            doNothing().when(funcionarioValidator).validarParaAtualizar(id, request);
            when(repository.findById(id)).thenReturn(Optional.of(funcionario));
            when(usuarioRepository.findByFuncionario(funcionario)).thenReturn(Optional.empty());
            when(funcionarioMapper.toResponse(funcionario)).thenReturn(responseEsperada);

            // Act
            FuncionarioResponse response = service.atualizar(id, request);

            // Assert
            assertNotNull(response);
            verify(funcionarioValidator).validarParaAtualizar(id, request);
            verify(funcionarioMapper).updateEntityFromDto(request, funcionario);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar funcionário inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            // Arrange
            UUID id = UUID.randomUUID();
            FuncionarioRequest request = criarRequestExemplo(Cargo.GERENTE);

            doNothing().when(funcionarioValidator).validarParaAtualizar(id, request);
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(id, request));

            verify(funcionarioValidator).validarParaAtualizar(id, request);
            verify(funcionarioMapper, never()).updateEntityFromDto(any(), any());
            verify(usuarioRepository, never()).findByFuncionario(any());
        }

        @Test
        @DisplayName("Não deve prosseguir com a atualização se a validação falhar")
        void naoDeveAtualizarSeValidacaoFalhar() {
            // Arrange
            UUID id = UUID.randomUUID();
            FuncionarioRequest request = criarRequestExemplo(Cargo.GERENTE);

            doThrow(new DadosJaCadastradosException("E-mail já cadastrado para outro funcionário"))
                    .when(funcionarioValidator).validarParaAtualizar(id, request);

            // Act & Assert
            assertThrows(DadosJaCadastradosException.class, () -> service.atualizar(id, request));

            verify(repository, never()).findById(any());
            verify(funcionarioMapper, never()).updateEntityFromDto(any(), any());
        }
    }

    @Nested
    @DisplayName("Testes de Exclusão (deletar)")
    class DeletarTests {

        @Test
        @DisplayName("Deve deletar funcionário quando ID for encontrado")
        void deveDeletarComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Funcionario funcionario = mock(Funcionario.class);

            when(repository.findById(id)).thenReturn(Optional.of(funcionario));

            // Act
            service.deletar(id);

            // Assert
            verify(repository).findById(id);
            verify(repository).delete(funcionario);
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