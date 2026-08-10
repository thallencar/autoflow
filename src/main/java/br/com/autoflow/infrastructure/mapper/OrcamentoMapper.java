package com.autoflow.infrastructure.mapper;

import com.autoflow.application.dto.OrcamentoRequest;
import com.autoflow.application.dto.OrcamentoResponse;
import com.autoflow.application.dto.OrcamentoItemRequest;
import com.autoflow.application.dto.OrcamentoItemResponse;
import com.autoflow.domain.model.Orcamento;
import com.autoflow.domain.model.OrcamentoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrcamentoMapper {

    OrcamentoResponse toResponse(Orcamento orcamento);

    @Mapping(source = "orcamento.id", target = "idOrcamento")
    OrcamentoItemResponse toResponse(OrcamentoItem orcamentoItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataCriacao", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "dataDecisao", ignore = true)
    Orcamento toEntity(OrcamentoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusReserva", ignore = true)
    @Mapping(target = "orcamento", ignore = true)
    OrcamentoItem toEntity(OrcamentoItemRequest request);
}