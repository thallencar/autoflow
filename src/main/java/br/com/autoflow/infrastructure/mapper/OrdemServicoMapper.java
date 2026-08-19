package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.MetricaOsResponse;
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
        @Mapping(target = "dataInicioExecucao", ignore = true)
        @Mapping(target = "dataFimExecucao", ignore = true)
        @Mapping(target = "dtEncerramentoOs", ignore = true)
        @Mapping(target = "dtReagendamentoOs", ignore = true)
        @Mapping(target = "dsMotivoCancelamento", ignore = true)
        @Mapping(target = "idsOrcamento", ignore = true)
        OrdemServico toEntity(OrdemServicoRequest request);

        OrdemServicoResponse toResponse(OrdemServico os);

        List<OrdemServicoResponse> toResponseList(List<OrdemServico> orders);

        default MetricaOsResponse toMetricaResponse(OrdemServico os) {
                if (os == null) {
                        return null;
                }

                return new MetricaOsResponse(
                        os.getIdOs(),
                        os.getStatusOS() != null ? os.getStatusOS().name() : null,
                        os.getTempoTotalEstimadoMinutos(),
                        os.getTempoTotalExecucaoMinutos(),
                        os.getDiferencaMinutos(),
                        os.getDataInicioExecucao(),
                        os.getDataFimExecucao()
                );
        }

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

        default UUID map(Orcamento orcamento) {
                if (orcamento == null) {
                        return null;
                }
                return orcamento.getId();
        }
}