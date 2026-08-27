package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EnderecoValidatorTest {

    @InjectMocks
    private EnderecoValidator validator;

    private EnderecoRequest criarRequestComUf(String uf) {
        return new EnderecoRequest(
                "Rua Principal", uf, "Novo Hamburgo", "Centro", "100", 93520000, "Apto 101"
        );
    }

    @Nested
    @DisplayName("Testes de Validação de UF")
    class ValidarUfTests {

        @ParameterizedTest
        @ValueSource(strings = {"RS", "SC", "PR", "SP", "RJ", "MG", "DF", "AC"})
        @DisplayName("Deve passar na validação quando a UF for válida")
        void deveValidarUfComSucesso(String ufValida) {
            EnderecoRequest request = criarRequestComUf(ufValida);

            assertDoesNotThrow(() -> validator.validarUf(request));
        }

        @ParameterizedTest
        @ValueSource(strings = {"XX", "INVALIDA", "RSA", "USA"})
        @DisplayName("Deve lançar exceção quando a UF for inválida")
        void deveFalharQuandoUfInvalida(String ufInvalida) {
            EnderecoRequest request = criarRequestComUf(ufInvalida);

            assertThrows(RegraNegocioException.class, () -> validator.validarUf(request));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Deve lançar exceção quando a UF for nula ou vazia")
        void deveFalharQuandoUfNulaOuVazia(String ufVazia) {
            EnderecoRequest request = criarRequestComUf(ufVazia);

            assertThrows(RegraNegocioException.class, () -> validator.validarUf(request));
        }
    }
}