package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.domain.model.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {EnderecoMapper.class})
public interface FuncionarioMapper {

    @Mapping(target = "idFuncionario", ignore = true)
    @Mapping(target = "ocupado", ignore = true)
    @Mapping(target = "nrAdvertencias", ignore = true)
    Funcionario toEntity(FuncionarioRequest request);

    @Mapping(target = "id", source = "idFuncionario")
    FuncionarioResponse toResponse(Funcionario funcionario);

    @Mapping(target = "idFuncionario", ignore = true)
    @Mapping(target = "ocupado", ignore = true)
    @Mapping(target = "nrAdvertencias", ignore = true)
    void updateEntityFromDto(FuncionarioRequest request, @MappingTarget Funcionario funcionario);
}