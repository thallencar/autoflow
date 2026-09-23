package br.com.autoflow.ports.inbound.veiculo;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;

public interface CriarVeiculoUseCase {
    VeiculoResponse criar(VeiculoRequest request);
}
