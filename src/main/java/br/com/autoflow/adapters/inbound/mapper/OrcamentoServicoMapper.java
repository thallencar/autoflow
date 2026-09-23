package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoServicoResponse;
import br.com.autoflow.domain.model.OrcamentoServico;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrcamentoServicoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itens", ignore = true)
    @Mapping(target = "servico.idServico", source = "idServico")
    @Mapping(target = "maoDeObra", source = "maoDeObra")
    @Mapping(target = "orcamento", ignore = true)
    OrcamentoServico toDomain(OrcamentoServicoRequest request);

    @Mapping(target = "idServico", source = "servico.idServico")
    @Mapping(target = "descricaoServico", source = "servico.dsServico")
    @Mapping(target = "maoDeObra", source = "maoDeObra")
    OrcamentoServicoResponse toResponse(OrcamentoServico entity);

    @AfterMapping
    default void vincularItens(@MappingTarget OrcamentoServico servico) {
        if (servico.getItens() != null) {
            // Ajuste caso utilize itens convertidos via domain
        }
    }
}