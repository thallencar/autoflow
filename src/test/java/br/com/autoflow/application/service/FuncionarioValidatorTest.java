package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioValidatorTest {

    @Mock
    private FuncionarioRepository repository;

    @InjectMocks
    private FuncionarioValidator validator;

    private FuncionarioRequest criarRequest(LocalDate dataNascimento, String cpf, String email) {
        EnderecoRequest enderecoRequest = new EnderecoRequest("Rua A", "RS", "Cidade C", "Bairro C", "123", 93500000, "casa");
        return new FuncionarioRequest(
                cpf, "Carlos Silva", "51999999999", email,
                Genero.MASCULINO, dataNascimento, Cargo.GERENTE, enderecoRequest
        );
    }

    @Nested
    @DisplayName("Validação de Criação de Funcionalidades (validarParaCriar)")
    class ValidarParaCriarTests {

        @Test
        @DisplayName("Deve validar com sucesso quando todos os dados forem válidos")
        void deveValidarComSucesso() {
            // Arrange
            LocalDate dataNascimentoValida = LocalDate.now().minusYears(20);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";
            FuncionarioRequest request = criarRequest(dataNascimentoValida, cpf, email);

            when(repository.existsByCpf(cpf)).thenReturn(false);
            when(repository.existsByEmail(email)).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> validator.validarParaCriar(request));

            verify(repository).existsByCpf(cpf);
            verify(repository).existsByEmail(email);
        }

        @Test
        @DisplayName("Deve lançar RegraNegocioException quando a data de nascimento for nula")
        void deveLancarExcecaoQuandoDataNascimentoForNula() {
            // Arrange
            FuncionarioRequest request = criarRequest(null, "12345678901", "carlos@gmail.com");

            // Act & Assert
            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> validator.validarParaCriar(request)
            );

            assertEquals("A data de nascimento é obrigatória.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar RegraNegocioException quando o funcionário tiver menos de 16 anos")
        void deveLancarExcecaoQuandoMenorDe16Anos() {
            // Arrange (15 anos atrás)
            LocalDate dataNascimentoInvalida = LocalDate.now().minusYears(15);
            FuncionarioRequest request = criarRequest(dataNascimentoInvalida, "12345678901", "carlos@gmail.com");

            // Act & Assert
            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> validator.validarParaCriar(request)
            );

            assertEquals("O funcionário deve ter no mínimo 16 anos.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve validar com sucesso quando o funcionário tiver exatamente 16 anos")
        void deveValidarQuandoTiverExatamente16Anos() {
            // Arrange (exatamente 16 anos atrás)
            LocalDate dataNascimentoExata = LocalDate.now().minusYears(16);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";
            FuncionarioRequest request = criarRequest(dataNascimentoExata, cpf, email);

            when(repository.existsByCpf(cpf)).thenReturn(false);
            when(repository.existsByEmail(email)).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> validator.validarParaCriar(request));
        }

        @Test
        @DisplayName("Deve lançar DadosJaCadastradosException quando o CPF já estiver cadastrado")
        void deveLancarExcecaoQuandoCpfJaExiste() {
            // Arrange
            LocalDate dataNascimentoValida = LocalDate.now().minusYears(20);
            String cpf = "12345678901";
            FuncionarioRequest request = criarRequest(dataNascimentoValida, cpf, "carlos@gmail.com");

            when(repository.existsByCpf(cpf)).thenReturn(true);

            // Act & Assert
            DadosJaCadastradosException exception = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> validator.validarParaCriar(request)
            );

            assertEquals("CPF já cadastrado: " + cpf, exception.getMessage());
            verify(repository).existsByCpf(cpf);
        }

        @Test
        @DisplayName("Deve lançar DadosJaCadastradosException quando o E-mail já estiver cadastrado")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            // Arrange
            LocalDate dataNascimentoValida = LocalDate.now().minusYears(20);
            String cpf = "12345678901";
            String email = "carlos@gmail.com";
            FuncionarioRequest request = criarRequest(dataNascimentoValida, cpf, email);

            when(repository.existsByCpf(cpf)).thenReturn(false);
            when(repository.existsByEmail(email)).thenReturn(true);

            // Act & Assert
            DadosJaCadastradosException exception = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> validator.validarParaCriar(request)
            );

            assertEquals("E-mail já cadastrado: " + email, exception.getMessage());
            verify(repository).existsByCpf(cpf);
            verify(repository).existsByEmail(email);
        }
    }
}