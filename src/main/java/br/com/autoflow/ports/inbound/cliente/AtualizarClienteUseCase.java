package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.domain.model.Cliente;
import java.util.UUID;

public interface AtualizarClienteUseCase {
    Cliente atualizar(UUID id, Cliente cliente);
}