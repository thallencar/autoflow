package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import java.util.UUID;

public interface AtualizarClienteUseCase {
    ClienteResponse atualizar(UUID id, ClienteUpdateRequest request);
}