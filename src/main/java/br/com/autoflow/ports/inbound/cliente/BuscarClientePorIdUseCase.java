package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import java.util.UUID;

public interface BuscarClientePorIdUseCase {
    ClienteResponse buscarPorId(UUID id);
}