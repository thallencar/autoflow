package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoItemEntity;
import br.com.autoflow.domain.model.OrcamentoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrcamentoItemEntityMapper {

    @Mapping(target = "orcamento", ignore = true)
    OrcamentoItemEntity toEntity(OrcamentoItem orcamentoItem);

    OrcamentoItem toDomain(OrcamentoItemEntity entity);
}