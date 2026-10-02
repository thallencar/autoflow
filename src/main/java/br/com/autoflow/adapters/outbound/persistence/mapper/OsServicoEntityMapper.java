package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OsServicoEntity;
import br.com.autoflow.domain.model.OsServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {OrdemServicoEntityMapper.class, ServicoEntityMapper.class})
public interface OsServicoEntityMapper {
    @Mapping(target = "ordemServico", qualifiedByName = "toEntityPadrao")
    OsServicoEntity toEntity(OsServico osServico);
    OsServico toDomain(OsServicoEntity entity);
}