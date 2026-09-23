package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.OsServicoResponse;
import br.com.autoflow.domain.model.OsServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ServicoMapper.class})
public interface OsServicoMapper {

    @Mapping(target = "id", source = "id")
    OsServicoResponse toResponse(OsServico osServico);
}