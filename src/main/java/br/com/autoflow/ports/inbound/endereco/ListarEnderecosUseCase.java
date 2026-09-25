package br.com.autoflow.ports.inbound.endereco;

import br.com.autoflow.domain.model.Endereco;

import java.util.List;

public interface ListarEnderecosUseCase {
    List<Endereco> listar();
}
