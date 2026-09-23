package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;

public interface BuscarClientePorDocumentoUseCase {
    ClienteResponse buscarPorDocumento(String documento);
}