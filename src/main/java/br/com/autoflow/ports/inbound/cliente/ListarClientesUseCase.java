package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import java.util.List;

public interface ListarClientesUseCase {
    List<ClienteResponse> listar();
}