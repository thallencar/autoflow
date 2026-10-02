package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.FuncionarioValidator;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.exception.DadosJaCadastradosException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioValidatorTest {

    @Mock
    private FuncionarioRepositoryPort repository;

    @InjectMocks
    private FuncionarioValidator validator;

    private Funcionario criarFuncionario(LocalDate dataNascimento, String cpf, String email) {
        Endereco endereco = new Endereco(UUID.randomUUID(),"Rua A", "123", "Bairro C", "Cidade C", "RS", 93500000, "casa" );
        return new Funcionario(
                UUID.randomUUID(), cpf, "Carlos Silva", "51999999999", email,
                Genero.MASCULINO, dataNascimento, Cargo.GERENTE, endereco, true,0
        );
    }

    @Nested
    @DisplayName("Validação de Criação de Funcionário (validarParaCriar)")
    class ValidarParaCriarTests {

        @Test
        @DisplayName("Deve validar com sucesso quando todos os dados forem válidos")
        void deveValidarComSucesso() {
            LocalDate dataNascimentoValida = LocalDate.now().minusYears(20);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";
            Funcionario funcionario = criarFuncionario(dataNascimentoValida, cpf, email);

            when(repository.existsByCpf(cpf)).thenReturn(false);
            when(repository.existsByEmail(email)).thenReturn(false);

            assertDoesNotThrow(() -> validator.validarParaCriar(funcionario));

            verify(repository).existsByCpf(cpf);
            verify(repository).existsByEmail(email);
        }

        @Test
        @DisplayName("Deve lançar RegraNegocioException quando a data de nascimento for nula")
        void deveLancarExcecaoQuandoDataNascimentoForNula() {
            Funcionario funcionario = criarFuncionario(null, "12345678901", "carlos@gmail.com");

            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> validator.validarParaCriar(funcionario)
            );

            assertEquals("A data de nascimento é obrigatória.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar RegraNegocioException quando o funcionário tiver menos de 16 anos")
        void deveLancarExcecaoQuandoMenorDe16Anos() {
            LocalDate dataNascimentoInvalida = LocalDate.now().minusYears(15);
            Funcionario funcionario = criarFuncionario(dataNascimentoInvalida, "12345678901", "carlos@gmail.com");

            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> validator.validarParaCriar(funcionario)
            );

            assertEquals("O funcionário deve ter no mínimo 16 anos.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve validar com sucesso quando o funcionário tiver exatamente 16 anos")
        void deveValidarQuandoTiverExatamente16Anos() {
            LocalDate dataNascimentoExata = LocalDate.now().minusYears(16);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";
            Funcionario funcionario = criarFuncionario(dataNascimentoExata, cpf, email);

            when(repository.existsByCpf(cpf)).thenReturn(false);
            when(repository.existsByEmail(email)).thenReturn(false);

            assertDoesNotThrow(() -> validator.validarParaCriar(funcionario));
        }

        @Test
        @DisplayName("Deve lançar DadosJaCadastradosException quando o CPF já estiver cadastrado")
        void deveLancarExcecaoQuandoCpfJaExiste() {
            LocalDate dataNascimentoValida = LocalDate.now().minusYears(20);
            String cpf = "12345678901";
            Funcionario funcionario = criarFuncionario(dataNascimentoValida, cpf, "carlos@gmail.com");

            when(repository.existsByCpf(cpf)).thenReturn(true);

            DadosJaCadastradosException exception = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> validator.validarParaCriar(funcionario)
            );

            assertEquals("CPF já cadastrado: " + cpf, exception.getMessage());
            verify(repository).existsByCpf(cpf);
        }

        @Test
        @DisplayName("Deve lançar DadosJaCadastradosException quando o E-mail já estiver cadastrado")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            LocalDate dataNascimentoValida = LocalDate.now().minusYears(20);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";
            Funcionario funcionario = criarFuncionario(dataNascimentoValida, cpf, email);

            when(repository.existsByCpf(cpf)).thenReturn(false);
            when(repository.existsByEmail(email)).thenReturn(true);

            DadosJaCadastradosException exception = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> validator.validarParaCriar(funcionario)
            );

            assertEquals("E-mail já cadastrado: " + email, exception.getMessage());
            verify(repository).existsByCpf(cpf);
            verify(repository).existsByEmail(email);
        }
    }

    @Nested
    @DisplayName("Validação de Atualização de Funcionário (validarParaAtualizar)")
    class ValidarParaAtualizarTests {

        @Test
        @DisplayName("Deve validar atualização com sucesso quando dados forem válidos ou pertencerem ao mesmo funcionário")
        void deveValidarAtualizacaoComSucesso() {
            UUID id = UUID.randomUUID();
            LocalDate dataNascimento = LocalDate.now().minusYears(25);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";

            Funcionario funcionario = criarFuncionario(dataNascimento, cpf, email);
            funcionario.setId(id);

            when(repository.findByCpf(cpf)).thenReturn(Optional.of(funcionario));
            when(repository.findByEmail(email)).thenReturn(Optional.of(funcionario));

            assertDoesNotThrow(() -> validator.validarParaAtualizar(id, funcionario));

            verify(repository).findByCpf(cpf);
            verify(repository).findByEmail(email);
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se o CPF já pertencer a outro funcionário")
        void deveLancarExcecaoQuandoCpfPertencerAOutro() {
            UUID idAtual = UUID.randomUUID();
            UUID idOutro = UUID.randomUUID();
            String cpf = "12345678901";

            Funcionario funcionario = criarFuncionario(LocalDate.now().minusYears(25), cpf, "carlos@gmail.com");
            funcionario.setId(idAtual);

            Funcionario outroFuncionario = criarFuncionario(LocalDate.now().minusYears(30), cpf, "outro@gmail.com");
            outroFuncionario.setId(idOutro);

            when(repository.findByCpf(cpf)).thenReturn(Optional.of(outroFuncionario));

            DadosJaCadastradosException exception = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> validator.validarParaAtualizar(idAtual, funcionario)
            );

            assertEquals("CPF já cadastrado para outro funcionário: " + cpf, exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se o e-mail já pertencer a outro funcionário")
        void deveLancarExcecaoQuandoEmailPertencerAOutro() {
            UUID idAtual = UUID.randomUUID();
            UUID idOutro = UUID.randomUUID();
            String email = "carlos@gmail.com";

            Funcionario funcionario = criarFuncionario(LocalDate.now().minusYears(25), "12345678901", email);
            funcionario.setId(idAtual);

            Funcionario outroFuncionario = criarFuncionario(LocalDate.now().minusYears(30), "98765432109", email);
            outroFuncionario.setId(idOutro);

            when(repository.findByCpf(funcionario.getCpf())).thenReturn(Optional.of(funcionario));
            when(repository.findByEmail(email)).thenReturn(Optional.of(outroFuncionario));

            DadosJaCadastradosException exception = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> validator.validarParaAtualizar(idAtual, funcionario)
            );

            assertEquals("E-mail já cadastrado para outro funcionário: " + email, exception.getMessage());
        }
    }
}