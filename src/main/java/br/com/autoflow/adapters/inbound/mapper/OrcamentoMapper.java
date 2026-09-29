package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.domain.model.Orcamento;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OrcamentoServicoMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrcamentoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataDecisao", ignore = true)
    @Mapping(target = "subtotalPecas", ignore = true)
    @Mapping(target = "maoObra", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "ordemServico", ignore = true) // Inserido via UseCase após buscar do banco
    @Mapping(target = "servicos", source = "servicos")
    Orcamento toDomain(OrcamentoRequest request);

    @Mapping(source = "ordemServico.idOs", target = "idOs")
    OrcamentoResponse toResponse(Orcamento orcamento);

    @AfterMapping
    default void vincularFilhos(@MappingTarget Orcamento orcamento) {
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().forEach(servico -> servico.associarOrcamento(orcamento));
        }
    }
}