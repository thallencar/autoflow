package com.autoflow.infrastructure.mapper;

import com.autoflow.application.dto.EstoqueRequest;
import com.autoflow.application.dto.EstoqueResponse;
import com.autoflow.domain.model.Estoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstoqueMapper {

    EstoqueResponse toResponse(Estoque estoque);

    @Mapping(target = "id", ignore = true)
    // Caso a request venha sem as quantidades, o @Builder.Default do lombok e a falta delas vai assumir '0' conforme mapeado na Entidade.
    Estoque toEntity(EstoqueRequest request);
}