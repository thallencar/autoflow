package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.application.dto.OrcamentoItemRequest;
import br.com.autoflow.application.dto.OrcamentoItemResponse;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
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