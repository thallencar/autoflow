package br.com.autoflow.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionCoverageTest {

    @Test
    void emailJaCadastradoExceptionMessage() {
        String email = "teste@dominio.com";
        EmailJaCadastradoException ex = new EmailJaCadastradoException(email);
        assertEquals("E-mail já cadastrado: " + email, ex.getMessage());
    }

    @Test
    void enderecoNaoEncontradoExceptionMessage() {
        UUID id = UUID.randomUUID();
        EnderecoNaoEncontradoException ex = new EnderecoNaoEncontradoException(id);
        assertEquals("Endereço não encontrado com o ID: " + id, ex.getMessage());
    }
}
