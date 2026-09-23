package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoServicoEntity;
import br.com.autoflow.domain.model.OrcamentoServico;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ServicoEntityMapper.class, OrcamentoItemEntityMapper.class, OrcamentoEntityMapper.class})
public interface OrcamentoServicoEntityMapper {
    OrcamentoServicoEntity toEntity(OrcamentoServico orcamentoServico);
    OrcamentoServico toDomain(OrcamentoServicoEntity entity);
}