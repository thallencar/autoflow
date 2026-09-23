package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.EstoqueEntity;
import br.com.autoflow.domain.model.Estoque;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EstoqueEntityMapper {
    EstoqueEntity toEntity(Estoque estoque);
    Estoque toDomain(EstoqueEntity entity);
}