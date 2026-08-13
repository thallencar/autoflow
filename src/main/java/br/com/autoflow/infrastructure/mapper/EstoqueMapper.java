package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.EstoqueRequest;
import br.com.autoflow.application.dto.EstoqueResponse;
import br.com.autoflow.domain.model.Estoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstoqueMapper {

    EstoqueResponse toResponse(Estoque estoque);

    @Mapping(target = "id", ignore = true)
    Estoque toEntity(EstoqueRequest request);
}