package br.com.autoflow.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveTratarEntidadeNaoEncontrada() {
        EntidadeNaoEncontradaException exception = new EntidadeNaoEncontradaException("Veículo", UUID.randomUUID());

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().status());
        assertTrue(response.getBody().message().contains("Veículo com ID"));
    }

    @Test
    void deveTratarDadosJaCadastrados() {
        DadosJaCadastradosException exception = new DadosJaCadastradosException("CPF duplicado");

        ResponseEntity<ApiErrorResponse> response = handler.handleCpf(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().status());
        assertEquals("CPF duplicado", response.getBody().message());
    }

    @Test
    void deveTratarMetodoArgumentNotValid() throws NoSuchMethodException {
        TestRequest request = new TestRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(new FieldError("request", "nome", "O nome é obrigatório"));

        Method method = GlobalExceptionHandler.class.getDeclaredMethod("handleValidation", MethodArgumentNotValidException.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                bindingResult
        );

        ResponseEntity<Map<String, String>> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("O nome é obrigatório", response.getBody().get("nome"));
    }

    @Test
    void deveTratarRegraNegocio() {
        RegraNegocioException exception = new RegraNegocioException("Regra inválida");

        ResponseEntity<ApiErrorResponse> response = handler.handleFuncionarioMenorDeIdade(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().status());
        assertEquals("Regra inválida", response.getBody().message());
    }

    private static class TestRequest {
        private String nome;
    }
}
