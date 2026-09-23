package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.domain.model.OrdemServico;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrdemServicoEntityMapper {

    OrdemServico toDomain(OrdemServicoEntity entity);

    OrdemServicoEntity toEntity(OrdemServico domain);

    List<OrdemServico> toDomainList(List<OrdemServicoEntity> entities);

    List<OrdemServicoEntity> toEntityList(List<OrdemServico> domains);

    void updateEntityFromDomain(@MappingTarget OrdemServicoEntity entity, OrdemServico domain);
}