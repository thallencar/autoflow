package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import br.com.autoflow.domain.model.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnderecoMapper {

    @Mapping(target = "id", ignore = true)
    Endereco toDomain(EnderecoRequest request);

    @Mapping(source = "id", target = "idEndereco")
    EnderecoResponse toResponse(Endereco endereco);

    @Mapping(target = "id", ignore = true)
    void updateDomainFromDto(EnderecoRequest request, @MappingTarget Endereco endereco);
}