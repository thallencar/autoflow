package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoItemRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoItemResponse;
import br.com.autoflow.domain.model.OrcamentoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrcamentoItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "valorTotal", ignore = true)
    @Mapping(target = "statusReserva", ignore = true)
    @Mapping(target = "orcamentoServico", ignore = true)
    @Mapping(target = "orcamento", ignore = true)
    OrcamentoItem toDomain(OrcamentoItemRequest request);

    @Mapping(source = "orcamentoServico.id", target = "idOrcamento")
    OrcamentoItemResponse toResponse(OrcamentoItem entity);
}