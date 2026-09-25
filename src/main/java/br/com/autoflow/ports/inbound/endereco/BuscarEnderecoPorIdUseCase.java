package br.com.autoflow.ports.inbound.endereco;

import br.com.autoflow.domain.model.Endereco;

import java.util.UUID;

public interface BuscarEnderecoPorIdUseCase {
    Endereco buscar(UUID id);
}
