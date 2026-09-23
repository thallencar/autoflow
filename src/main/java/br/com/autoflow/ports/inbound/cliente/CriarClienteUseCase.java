package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;

public interface CriarClienteUseCase {
    ClienteResponse criar(ClienteRequest request);
}