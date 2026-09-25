package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.VeiculoEntity;
import br.com.autoflow.domain.model.Veiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VeiculoEntityMapper {

    @Mapping(target = "clienteId", source = "clienteId")
    Veiculo toDomain(VeiculoEntity entity);

    @Mapping(target = "clienteId", source = "clienteId")
    VeiculoEntity toEntity(Veiculo domain);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDomain(Veiculo domain, @MappingTarget VeiculoEntity entity);
}