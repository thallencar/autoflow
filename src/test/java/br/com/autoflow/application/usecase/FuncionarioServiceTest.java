package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.FuncionarioValidator;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.exception.DadosJaCadastradosException;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
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
    private FuncionarioRepositoryPort repository;

    @Mock
    private EnderecoRepositoryPort enderecoRepository;

    @Mock
    private FuncionarioValidator funcionarioValidator;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private FuncionarioUseCaseImpl service;

    private Funcionario criarFuncionarioExemplo(Cargo cargo) {
        Endereco endereco = new Endereco(UUID.randomUUID(), "Rua A", "RS", "casa", "Bairro C", "Cidade C", 123, "93500000");
        return new Funcionario(
                UUID.randomUUID(), "12345678901", "Carlos Silva", "51999999999", "carlos@gmail.com",
                Genero.MASCULINO, LocalDate.of(2000, 9, 12), cargo,  endereco,false,0
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
            Funcionario funcionario = criarFuncionarioExemplo(cargo);

            doNothing().when(funcionarioValidator).validarParaCriar(funcionario);
            when(repository.save(funcionario)).thenReturn(funcionario);
            when(passwordEncoder.encode(funcionario.getCpf())).thenReturn("encodedPass");

            // Act
            Funcionario resultado = service.criar(funcionario);

            // Assert
            assertNotNull(resultado);
            assertEquals(funcionario, resultado);

            // Verificações dos serviços chamados
            verify(funcionarioValidator).validarParaCriar(funcionario);
            verify(repository).save(funcionario);

            // Captura do usuário salvo para garantir que foi associado
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(usuarioCaptor.capture());
            assertNotNull(usuarioCaptor.getValue());
        }

        @Test
        @DisplayName("Não deve salvar entidades se a validação lançar exceção")
        void naoDeveCriarSeValidacaoFalhar() {
            // Arrange
            Funcionario funcionario = criarFuncionarioExemplo(Cargo.GERENTE);
            doThrow(new DadosJaCadastradosException("CPF já cadastrado"))
                    .when(funcionarioValidator).validarParaCriar(funcionario);

            // Act & Assert
            assertThrows(DadosJaCadastradosException.class, () -> service.criar(funcionario));

            verify(enderecoRepository, never()).findByCepAndNumero(any(), any());
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
            Funcionario f1 = criarFuncionarioExemplo(Cargo.GERENTE);
            Funcionario f2 = criarFuncionarioExemplo(Cargo.MECANICO);

            when(repository.findAll()).thenReturn(List.of(f1, f2));

            // Act
            List<Funcionario> resultado = service.listar();

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
            List<Funcionario> resultado = service.listar();

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
            Funcionario funcionario = criarFuncionarioExemplo(Cargo.GERENTE);

            when(repository.findById(id)).thenReturn(Optional.of(funcionario));

            // Act
            Funcionario resultado = service.buscarPorId(id);

            // Assert
            assertNotNull(resultado);
            assertEquals(funcionario, resultado);
            verify(repository).findById(id);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando ID não existir")
        void deveLancarExcecaoQuandoIdNaoEncontrado() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(id));
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
            Funcionario funcionarioParam = criarFuncionarioExemplo(cargo);
            Funcionario funcionarioExistente = criarFuncionarioExemplo(Cargo.AUXILIAR_MECANICO);

            Usuario usuarioMock = mock(Usuario.class);

            doNothing().when(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);
            when(repository.findById(id)).thenReturn(Optional.of(funcionarioExistente));
            when(usuarioRepository.findByFuncionarioId(id)).thenReturn(Optional.of(usuarioMock));

            // Act
            Funcionario resultado = service.atualizar(id, funcionarioParam);

            // Assert
            assertNotNull(resultado);
            verify(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);
            verify(repository).findById(id);
            verify(repository).save(funcionarioExistente);
            verify(usuarioRepository).findByFuncionarioId(id);
            verify(usuarioMock).atualizarDadosAcesso(funcionarioParam.getEmail(), perfilEsperado);
            verify(usuarioRepository).save(usuarioMock);
        }

        @Test
        @DisplayName("Deve atualizar funcionário com sucesso mesmo quando não houver usuário vinculado")
        void deveAtualizarApenasFuncionarioQuandoNaoHouverUsuario() {
            // Arrange
            UUID id = UUID.randomUUID();
            Funcionario funcionarioParam = criarFuncionarioExemplo(Cargo.MECANICO);
            Funcionario funcionarioExistente = criarFuncionarioExemplo(Cargo.MECANICO);

            doNothing().when(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);
            when(repository.findById(id)).thenReturn(Optional.of(funcionarioExistente));
            when(usuarioRepository.findByFuncionarioId(id)).thenReturn(Optional.empty());

            // Act
            Funcionario resultado = service.atualizar(id, funcionarioParam);

            // Assert
            assertNotNull(resultado);
            verify(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);
            verify(repository).save(funcionarioExistente);
            verify(usuarioRepository).findByFuncionarioId(id);
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar funcionário inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            // Arrange
            UUID id = UUID.randomUUID();
            Funcionario funcionarioParam = criarFuncionarioExemplo(Cargo.GERENTE);

            doNothing().when(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(id, funcionarioParam));

            verify(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve prosseguir com a atualização se a validação falhar")
        void naoDeveAtualizarSeValidacaoFalhar() {
            // Arrange
            UUID id = UUID.randomUUID();
            Funcionario funcionarioParam = criarFuncionarioExemplo(Cargo.GERENTE);

            doThrow(new DadosJaCadastradosException("E-mail já cadastrado para outro funcionário"))
                    .when(funcionarioValidator).validarParaAtualizar(id, funcionarioParam);

            // Act & Assert
            assertThrows(DadosJaCadastradosException.class, () -> service.atualizar(id, funcionarioParam));

            verify(repository, never()).findById(any());
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Exclusão (deletar)")
    class DeletarTests {

        @Test
        @DisplayName("Deve deletar funcionário e usuário quando ID for encontrado")
        void deveDeletarComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Funcionario funcionario = criarFuncionarioExemplo(Cargo.GERENTE);
            Usuario usuario = mock(Usuario.class);

            when(repository.findById(id)).thenReturn(Optional.of(funcionario));
            when(usuarioRepository.findByFuncionarioId(id)).thenReturn(Optional.of(usuario));

            // Act
            service.deletar(id);

            // Assert
            verify(repository).findById(id);
            verify(usuarioRepository).findByFuncionarioId(id);
            verify(usuarioRepository).delete(usuario);
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