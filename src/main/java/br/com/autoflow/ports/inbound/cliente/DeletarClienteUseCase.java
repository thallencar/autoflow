package br.com.autoflow.ports.inbound.cliente;

import java.util.UUID;

public interface DeletarClienteUseCase {
    void deletar(UUID id);
}