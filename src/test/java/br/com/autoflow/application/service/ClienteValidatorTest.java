package br.com.autoflow.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.domain.enums.Genero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.RegraNegocioException;

@ExtendWith(MockitoExtension.class)
class ClienteValidatorTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteValidator validator;

    @Test
    @DisplayName("Deve passar na validação quando todos os dados forem válidos")
    void validarComSucesso() {
        EnderecoRequest enderecoRequest = new EnderecoRequest(
                "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, "Apto 101"
        );
        ClienteRequest request = new ClienteRequest(
                "Teste da Silva", "87032522726", "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, enderecoRequest
        );

        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByDocumento(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarParaCriar(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se o cliente for menor de idade")
    void deveFalharMenorDeIdade() {
        EnderecoRequest enderecoRequest = new EnderecoRequest(
                "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, "Apto 101"
        );
        ClienteRequest request = new ClienteRequest(
                "Menor de Idade", "04935216064", "menor@email.com", LocalDate.now().minusYears(10), "51999999999", Genero.OUTROS, enderecoRequest
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se o CPF for inválido")
    void deveFalharCpfInvalido() {
        EnderecoRequest enderecoRequest = new EnderecoRequest(
                "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, "Apto 101"
        );
        ClienteRequest request = new ClienteRequest(
                "Teste da Silva", "11111111111", "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, enderecoRequest
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(request));
    }

    @Test
    @DisplayName("Deve lançar exceção se o e-mail já estiver cadastrado")
    void deveFalharEmailDuplicado() {
        EnderecoRequest enderecoRequest2 = new EnderecoRequest(
                "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, "Apto 101"
        );

        ClienteRequest request2 = new ClienteRequest(
                "Teste da Silva", "87032522726", "existente@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, enderecoRequest2
        );

        when(repository.existsByEmail("existente@email.com")).thenReturn(true);
        when(repository.existsByDocumento(anyString())).thenReturn(false);

        assertThrows(DadosJaCadastradosException.class, () -> validator.validarParaCriar(request2));
    }
}