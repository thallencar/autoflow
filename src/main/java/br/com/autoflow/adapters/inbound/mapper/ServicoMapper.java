package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ServicoResponse;
import br.com.autoflow.domain.model.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ServicoMapper {

    @Mapping(target = "idServico", ignore = true)
    Servico toDomain(ServicoRequest request);

    ServicoResponse toResponse(Servico servico);

    @Mapping(target = "idServico", ignore = true)
    void updateDomainFromDto(ServicoRequest request, @MappingTarget Servico servico);
}