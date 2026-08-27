package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.OrcamentoServicoRequest;
import br.com.autoflow.application.dto.OrcamentoServicoResponse;
import br.com.autoflow.application.dto.OrcamentoItemResponse;
import br.com.autoflow.domain.model.OrcamentoServico;
import br.com.autoflow.domain.model.OrcamentoItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrcamentoServicoMapper {

    @Mapping(source = "orcamentoServico.id", target = "idOrcamento")
    OrcamentoItemResponse toResponse(OrcamentoItem orcamentoItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itens", ignore = true)
    @Mapping(target = "servico.idServico", source = "idServico")
    @Mapping(target = "maoDeObra", source = "maoDeObra")
    @Mapping(target = "orcamento", ignore = true)
    OrcamentoServico toEntity(OrcamentoServicoRequest request);

    @Mapping(target = "idServico", source = "servico.idServico")
    @Mapping(target = "descricaoServico", source = "servico.dsServico")
    @Mapping(target = "maoDeObra", source = "maoDeObra")
    OrcamentoServicoResponse toResponse(OrcamentoServico entity);

    @AfterMapping
    default void vincularItens(@MappingTarget OrcamentoServico servico) {
        if (servico.getItens() != null) {
            servico.getItens().forEach(item -> item.setOrcamentoServico(servico));
        }
    }
}
