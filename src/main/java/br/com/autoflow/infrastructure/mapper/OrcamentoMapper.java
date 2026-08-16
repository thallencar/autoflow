package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.OrcamentoItemRequest;
import br.com.autoflow.application.dto.OrcamentoItemResponse;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
import br.com.autoflow.domain.model.OrdemServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {StatusOrcamento.class, LocalDateTime.class, OrdemServico.class})
public interface OrcamentoMapper {

    @Mapping(source = "ordemServico.idOs", target = "idOs")
    OrcamentoResponse toResponse(Orcamento orcamento);

    @Mapping(source = "orcamento.id", target = "idOrcamento")
    OrcamentoItemResponse toResponse(OrcamentoItem orcamentoItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ordemServico", expression = "java(mapOrdemServicoPorId(request.idOs()))") // <-- USO DA EXPR ESSÃO DIRETA
    @Mapping(target = "status", expression = "java(StatusOrcamento.PENDENTE)")
    @Mapping(target = "dataCriacao", expression = "java(LocalDateTime.now())")
    @Mapping(target = "dataDecisao", ignore = true)
    Orcamento toEntity(OrcamentoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusReserva", ignore = true)
    @Mapping(target = "orcamento", ignore = true)
    OrcamentoItem toEntity(OrcamentoItemRequest request);

    default OrdemServico mapOrdemServicoPorId(UUID idOs) {
        if (idOs == null) {
            return null;
        }
        return OrdemServico.builder()
                .idOs(idOs)
                .build();
    }
}