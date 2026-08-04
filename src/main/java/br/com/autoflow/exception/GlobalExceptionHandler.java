package br.com.autoflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FuncionarioNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            FuncionarioNaoEncontradoException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(CpfJaCadastradoException.class)
    public ResponseEntity<ApiErrorResponse> handleCpf(
            CpfJaCadastradoException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ApiErrorResponse> handleEmail(
            EmailJaCadastradoException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(EnderecoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleEnderecoNaoEncontrado(
            EnderecoNaoEncontradoException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }
    @ExceptionHandler(FuncionarioMenorDeIdadeException.class)
    public ResponseEntity<ApiErrorResponse> handleFuncionarioMenorDeIdade(FuncionarioMenorDeIdadeException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
                );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}