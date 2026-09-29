package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.domain.model.OrdemServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {OrcamentoEntityMapper.class, OsServicoEntityMapper.class}
)
public interface OrdemServicoEntityMapper {

    @Mapping(target = "idsOrcamento", source = "orcamentos")
    OrdemServico toDomain(OrdemServicoEntity entity);

    @Mapping(target = "orcamentos", source = "idsOrcamento")
    OrdemServicoEntity toEntity(OrdemServico domain);

    List<OrdemServico> toDomainList(List<OrdemServicoEntity> entities);

    //List<OrdemServicoEntity> toEntityList(List<OrdemServico> domains);

    void updateEntityFromDomain(@MappingTarget OrdemServicoEntity entity, OrdemServico domain);

    default OrdemServicoEntity toEntityComVinculo(OrdemServico domain) {
        OrdemServicoEntity entity = toEntity(domain); // Chama o gerado pelo MapStruct
        if (entity != null && entity.getOrcamentos() != null) {
            for (OrcamentoEntity orcamentoEntity : entity.getOrcamentos()) {
                orcamentoEntity.setOrdemServico(entity);
            }
        }
        return entity;
    }
}