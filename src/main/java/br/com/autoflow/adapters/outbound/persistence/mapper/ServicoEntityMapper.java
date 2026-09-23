package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.ServicoEntity;
import br.com.autoflow.domain.model.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ServicoEntityMapper {

    ServicoEntity toEntity(Servico servico);

    Servico toDomain(ServicoEntity entity);

    void updateEntityFromDomain(Servico servico, @MappingTarget ServicoEntity entity);
}