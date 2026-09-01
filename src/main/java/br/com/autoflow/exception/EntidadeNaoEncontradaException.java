package br.com.autoflow.exception;

import java.util.UUID;

public class EntidadeNaoEncontradaException extends RuntimeException {

    public EntidadeNaoEncontradaException (String entidade, UUID id) {
        super(String.format("%s com ID %s nao encontrado.", entidade, id));
    }
}