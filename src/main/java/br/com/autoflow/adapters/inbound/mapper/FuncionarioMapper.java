package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioRequest;
import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioResponse;
import br.com.autoflow.domain.model.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget; // <--- Importante

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {EnderecoMapper.class})
public interface FuncionarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ocupado", ignore = true)
    @Mapping(target = "nrAdvertencias", ignore = true)
    Funcionario toDomain(FuncionarioRequest request);

    @Mapping(target = "id", source = "id")
    FuncionarioResponse toResponse(Funcionario funcionario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ocupado", ignore = true)
    @Mapping(target = "nrAdvertencias", ignore = true)
    void updateEntityFromDto(FuncionarioRequest request, @MappingTarget Funcionario funcionario);
}