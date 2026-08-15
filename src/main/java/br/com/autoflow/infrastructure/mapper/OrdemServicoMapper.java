package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrdemServicoMapper {

        @Mapping(target = "idOs", ignore = true)
        @Mapping(target = "statusOS", constant = "RECEBIDA")
        @Mapping(target = "dtAberturaOs", ignore = true)
        @Mapping(target = "dtInicioDiagnostico", ignore = true)
        @Mapping(target = "dtFimDiagnostico", ignore = true)
        @Mapping(target = "dtAprovacaoOrcamento", ignore = true)
        @Mapping(target = "dtEncerramentoOs", ignore = true)
        @Mapping(target = "dtReagendamentoOs", ignore = true)
        @Mapping(target = "dsMotivoCancelamento", ignore = true)
        @Mapping(target = "idsOrcamento", ignore = true)
        OrdemServico toEntity(OrdemServicoRequest request);

        OrdemServicoResponse toResponse(OrdemServico os);

        List<OrdemServicoResponse> toResponseList(List<OrdemServico> orders);

        @Mapping(target = "idOs", ignore = true)
        @Mapping(target = "statusOS", ignore = true)
        @Mapping(target = "dtAberturaOs", ignore = true)
        @Mapping(target = "stTermoAceito", ignore = true)
        @Mapping(target = "dtAceiteTermo", ignore = true)
        @Mapping(target = "dtInicioDiagnostico", ignore = true)
        @Mapping(target = "dtFimDiagnostico", ignore = true)
        @Mapping(target = "dtAprovacaoOrcamento", ignore = true)
        @Mapping(target = "dtEncerramentoOs", ignore = true)
        @Mapping(target = "dtReagendamentoOs", ignore = true)
        @Mapping(target = "dsMotivoCancelamento", ignore = true)
        @Mapping(target = "idsOrcamento", ignore = true)
        void updateEntityFromRequest(@MappingTarget OrdemServico os, OrdemServicoRequest request);

        // O MapStruct usará este método automaticamente para converter a lista de Orcamento para UUID
        default UUID map(Orcamento orcamento) {
                if (orcamento == null) {
                        return null;
                }
                return orcamento.getId(); // Certifique-se do nome exato do getter do ID em Orcamento
        }
}