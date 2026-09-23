package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoEntity;
import br.com.autoflow.domain.model.Orcamento;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {OrdemServicoEntityMapper.class, OrcamentoServicoEntityMapper.class, OrcamentoItemEntityMapper.class})
public interface OrcamentoEntityMapper {
    OrcamentoEntity toEntity(Orcamento orcamento);
    Orcamento toDomain(OrcamentoEntity entity);
}
