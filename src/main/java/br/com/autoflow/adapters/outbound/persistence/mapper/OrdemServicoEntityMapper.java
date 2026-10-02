package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.domain.model.OrdemServico;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {OrcamentoEntityMapper.class}
)
public interface OrdemServicoEntityMapper {

    @Mapping(target = "idsOrcamento", source = "orcamentos")
    OrdemServico toDomain(OrdemServicoEntity entity);

    @Named("toEntityPadrao")
    @Mapping(target = "orcamentos", source = "idsOrcamento")
    OrdemServicoEntity toEntity(OrdemServico domain);

    List<OrdemServico> toDomainList(List<OrdemServicoEntity> entities);

    void updateEntityFromDomain(@MappingTarget OrdemServicoEntity entity, OrdemServico domain);

    default OrdemServicoEntity toEntityComVinculo(OrdemServico domain) {
        OrdemServicoEntity entity = toEntity(domain);
        if (entity != null && entity.getOrcamentos() != null) {
            for (OrcamentoEntity orcamentoEntity : entity.getOrcamentos()) {
                orcamentoEntity.setOrdemServico(entity);
            }
        }
        return entity;
    }
}