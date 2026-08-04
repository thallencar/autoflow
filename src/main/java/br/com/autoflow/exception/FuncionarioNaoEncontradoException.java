package br.com.autoflow.exception;

import java.util.UUID;

public class FuncionarioNaoEncontradoException
        extends RuntimeException {

    public FuncionarioNaoEncontradoException(UUID id) {
        super("Funcionário não encontrado: " + id);
    }
}