package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueResponse;
import br.com.autoflow.domain.model.Estoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EstoqueMapper {

    @Mapping(target = "id", ignore = true)
    Estoque toDomain(EstoqueRequest request);

    EstoqueResponse toResponse(Estoque estoque);
}