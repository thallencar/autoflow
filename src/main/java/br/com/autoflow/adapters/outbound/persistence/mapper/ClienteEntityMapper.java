package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.autoflow.domain.model.Cliente;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClienteEntityMapper {
    Cliente toDomain(ClienteEntity entity);
    ClienteEntity toEntity(Cliente domain);
}