package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.domain.model.Cliente;

import java.util.UUID;

public interface BuscarClientePorIdUseCase {
    Cliente buscarPorId(UUID id);
}