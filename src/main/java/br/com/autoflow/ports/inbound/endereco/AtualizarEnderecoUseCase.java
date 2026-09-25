package br.com.autoflow.ports.inbound.endereco;

import br.com.autoflow.domain.model.Endereco;

import java.util.UUID;

public interface AtualizarEnderecoUseCase {
    Endereco atualizar(UUID id, Endereco endereco);
}
