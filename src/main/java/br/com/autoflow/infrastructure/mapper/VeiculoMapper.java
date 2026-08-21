package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.application.dto.VeiculoResponse;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeiculoMapper {

    private final ClienteMapper clienteMapper;

    public Veiculo toEntity(VeiculoRequest request, Cliente cliente) {
        return Veiculo.builder()
                .placa(request.placa() != null ? request.placa().toUpperCase().trim() : null)
                .marca(request.marca())
                .modelo(request.modelo())
                .kmAtual(request.kmAtual())
                .anoFabricacao(request.anoFabricacao())
                .cor(request.cor())
                .cliente(cliente)
                .build();
    }

    public void updateEntity(VeiculoRequest request, Veiculo veiculo, Cliente cliente) {
        if (request.placa() != null) {
            veiculo.setPlaca(request.placa().toUpperCase().trim());
        }
        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAnoFabricacao(request.anoFabricacao());
        veiculo.setCor(request.cor());
        veiculo.setCliente(cliente);
    }

    public VeiculoResponse toResponse(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getKmAtual(),
                veiculo.getAnoFabricacao(),
                veiculo.getCor(),
                veiculo.getCliente() != null ? veiculo.getCliente().getId() : null
        );
    }
}