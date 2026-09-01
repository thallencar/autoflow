package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
import br.com.autoflow.domain.model.OrcamentoServico;
import br.com.autoflow.domain.model.OrdemServico;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {StatusOrcamento.class, LocalDateTime.class, OrdemServico.class,OsServicoMapper.class})
public interface OrcamentoMapper {

    @Mapping(source = "ordemServico.idOs", target = "idOs")
    OrcamentoResponse toResponse(Orcamento orcamento);

    @Mapping(source = "orcamentoServico.id", target = "idOrcamento")
    OrcamentoItemResponse toResponse(OrcamentoItem orcamentoItem);

    @Mapping(source = "servico.idServico", target = "idServico")
    @Mapping(source = "servico.dsServico", target = "descricaoServico")
    OrcamentoServicoResponse toResponse(OrcamentoServico orcamentoServico);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataDecisao", ignore = true)
    @Mapping(target = "subtotalPecas", ignore = true)
    @Mapping(target = "maoObra", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "ordemServico", ignore = true)
    Orcamento toEntity(OrcamentoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "valorTotal", ignore = true)
    @Mapping(target = "statusReserva", ignore = true)
    @Mapping(target = "orcamentoServico", ignore = true)
    OrcamentoItem toEntity(OrcamentoItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "idServico", target = "servico.idServico")
    @Mapping(source = "maoDeObra", target = "maoDeObra")
    OrcamentoServico toEntity(OrcamentoServicoRequest request);

    @AfterMapping
    default void vincularFilhos(@MappingTarget Orcamento orcamento) {
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().forEach(servico -> {
                servico.setOrcamento(orcamento);

                if (servico.getItens() != null) {
                    servico.getItens().forEach(item -> item.setOrcamentoServico(servico)); // Amarra Item -> OrcamentoServico
                }
            });
        }
    }
}