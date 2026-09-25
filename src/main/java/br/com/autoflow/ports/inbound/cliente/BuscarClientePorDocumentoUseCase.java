package br.com.autoflow.ports.inbound.cliente;

import br.com.autoflow.domain.model.Cliente;

public interface BuscarClientePorDocumentoUseCase {
    Cliente buscarPorDocumento(String documento);
}