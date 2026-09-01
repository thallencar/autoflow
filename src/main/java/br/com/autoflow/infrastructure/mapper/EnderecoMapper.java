package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.EnderecoResponse;
import br.com.autoflow.domain.model.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnderecoMapper {

    @Mapping(target = "id", ignore = true)
    Endereco toEntity(EnderecoRequest request);

    @Mapping(source = "id", target = "idEndereco")
    EnderecoResponse toResponse(Endereco endereco);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(EnderecoRequest request, @MappingTarget Endereco endereco);
}