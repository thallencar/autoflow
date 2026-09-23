package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.FuncionarioEntity;
import br.com.autoflow.domain.model.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {EnderecoEntityMapper.class})
public interface FuncionarioEntityMapper {
    FuncionarioEntity toEntity(Funcionario funcionario);
    Funcionario toDomain(FuncionarioEntity entity);
}