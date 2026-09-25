package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;
import br.com.autoflow.domain.model.Veiculo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", source = "clienteId")
    @Mapping(target = "placa", expression = "java(request.placa() != null ? request.placa().toUpperCase() : null)")
    Veiculo toDomain(VeiculoRequest request);

    @Mapping(target = "clienteId", source = "clienteId")
    VeiculoResponse toResponse(Veiculo domain);
}