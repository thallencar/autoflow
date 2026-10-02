package br.com.autoflow.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import br.com.autoflow.application.validator.ClienteValidator;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.autoflow.domain.exception.DadosJaCadastradosException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class ClienteValidatorTest {

    @Mock
    private ClienteRepositoryPort repository;

    @InjectMocks
    private ClienteValidator validator;

    private Endereco criarEnderecoPadrao() {
        return new Endereco(
                UUID.randomUUID(), "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, "Apto 101"
        );
    }

    @Test
    @DisplayName("Deve passar na validação quando todos os dados forem válidos")
    void validarComSucesso() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Teste da Silva", "87032522726", "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByDocumento(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se o cliente for menor de idade")
    void deveFalharMenorDeIdade() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Menor de Idade", "04935216064", "menor@email.com", LocalDate.now().minusYears(10), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se o CPF for inválido")
    void deveFalharCpfInvalido() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Teste da Silva", "11111111111", "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se o e-mail já estiver cadastrado")
    void deveFalharEmailDuplicado() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Teste da Silva", "87032522726", "existente@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        when(repository.existsByEmail("existente@email.com")).thenReturn(true);
        when(repository.existsByDocumento(anyString())).thenReturn(false);

        assertThrows(DadosJaCadastradosException.class, () -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se a data de nascimento for nula")
    void deveFalharDataNascimentoNula() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Teste da Silva", "87032522726", "teste@email.com", null, "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se o documento for nulo ou vazio")
    void deveFalharDocumentoNuloOuVazio() {
        Cliente clienteNulo = new Cliente(
                UUID.randomUUID(), "Teste da Silva", null, "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(clienteNulo));
    }

    @Test
    @DisplayName("Deve lançar exceção se o tamanho do documento for inválido")
    void deveFalharTamanhoDocumentoInvalido() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Teste da Silva", "123", "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve passar na validação quando o CNPJ for válido")
    void validarCnpjComSucesso() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Empresa LTDA", "06462098000185", "empresa@email.com", LocalDate.of(1990, 1, 1), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByDocumento(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se o CNPJ for inválido")
    void deveFalharCnpjInvalido() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Empresa LTDA", "11111111111111", "empresa@email.com", LocalDate.of(1990, 1, 1), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        assertThrows(RegraNegocioException.class, () -> validator.validarParaCriar(cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção se o documento já estiver cadastrado")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void deveFalharDocumentoDuplicado() {
        Cliente cliente = new Cliente(
                UUID.randomUUID(), "Teste da Silva", "87032522726", "teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, criarEnderecoPadrao()
        );

        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByDocumento(anyString())).thenReturn(true);

        assertThrows(DadosJaCadastradosException.class, () -> validator.validarParaCriar(cliente));
    }
}