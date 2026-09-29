package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoServicoEntity;
import br.com.autoflow.domain.model.OrcamentoServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ServicoEntityMapper.class, OrcamentoItemEntityMapper.class})
public interface OrcamentoServicoEntityMapper {

    @Mapping(target = "orcamento", ignore = true)
    OrcamentoServicoEntity toEntity(OrcamentoServico orcamentoServico);

    @Mapping(target = "orcamento", ignore = true)
    OrcamentoServico toDomain(OrcamentoServicoEntity entity);
}