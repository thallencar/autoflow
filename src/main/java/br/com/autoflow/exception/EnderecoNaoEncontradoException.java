package br.com.autoflow.exception;

import java.util.UUID;

public class EnderecoNaoEncontradoException extends RuntimeException {

    public EnderecoNaoEncontradoException(UUID idEndereco) {
        super("Endereço não encontrado com o ID: " + idEndereco);
    }
}