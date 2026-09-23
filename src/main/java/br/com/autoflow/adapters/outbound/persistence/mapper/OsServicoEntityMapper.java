package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OsServicoEntity;
import br.com.autoflow.domain.model.OsServico;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {OrdemServicoEntityMapper.class, ServicoEntityMapper.class})
public interface OsServicoEntityMapper {
    OsServicoEntity toEntity(OsServico osServico);
    OsServico toDomain(OsServicoEntity entity);
}