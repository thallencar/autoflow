package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;
import br.com.autoflow.domain.model.Veiculo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", source = "clienteId") // Mapeia direto do request
    @Mapping(target = "placa", expression = "java(request.placa() != null ? request.placa().toUpperCase() : null)")
    Veiculo toDomain(VeiculoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "placa", ignore = true)
    @Mapping(target = "marca", ignore = true)
    @Mapping(target = "modelo", ignore = true)
    @Mapping(target = "kmAtual", ignore = true)
    @Mapping(target = "anoFabricacao", ignore = true)
    @Mapping(target = "cor", ignore = true)
    void updateEntityFromDto(VeiculoRequest request, @MappingTarget Veiculo veiculo);

    @Mapping(target = "clienteId", source = "clienteId")
    VeiculoResponse toResponse(Veiculo domain);
}