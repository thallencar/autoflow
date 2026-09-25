package br.com.autoflow.ports.inbound.endereco;

import br.com.autoflow.domain.model.Endereco;

public interface CriarEnderecoUseCase {
    Endereco criar(Endereco endereco);
}
