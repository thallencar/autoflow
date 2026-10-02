package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.EnderecoValidator;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EnderecoValidatorTest {

    @InjectMocks
    private EnderecoValidator validator;

    private Endereco criarEnderecoComUf(String uf) {
        return new Endereco(
                UUID.randomUUID(),
                "93520-000",
                uf,
                "Novo Hamburgo",
                "Centro",
                "Rua Principal",
                100,
                "Apto 101"
        );
    }

    @Nested
    @DisplayName("Testes de Validação de UF")
    class ValidarUfTests {

        @ParameterizedTest
        @ValueSource(strings = {"RS", "SC", "PR", "SP", "RJ", "MG", "DF", "AC"})
        @DisplayName("Deve passar na validação quando a UF for válida")
        void deveValidarUfComSucesso(String ufValida) {
            Endereco endereco = criarEnderecoComUf(ufValida);

            assertDoesNotThrow(() -> validator.validarUf(endereco));
        }

        @ParameterizedTest
        @ValueSource(strings = {"XX", "INVALIDA", "RSA", "USA"})
        @DisplayName("Deve lançar exceção quando a UF for inválida")
        void deveFalharQuandoUfInvalida(String ufInvalida) {
            Endereco endereco = criarEnderecoComUf(ufInvalida);

            assertThrows(RegraNegocioException.class, () -> validator.validarUf(endereco));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Deve lançar exceção quando a UF for nula ou vazia")
        void deveFalharQuandoUfNulaOuVazia(String ufVazia) {
            Endereco endereco = criarEnderecoComUf(ufVazia);

            assertThrows(RegraNegocioException.class, () -> validator.validarUf(endereco));
        }
    }
}