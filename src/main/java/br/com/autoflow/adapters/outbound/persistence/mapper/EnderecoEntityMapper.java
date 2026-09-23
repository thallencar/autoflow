package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.EnderecoEntity;
import br.com.autoflow.domain.model.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnderecoEntityMapper {

    EnderecoEntity toEntity(Endereco endereco);

    Endereco toDomain(EnderecoEntity entity);
}