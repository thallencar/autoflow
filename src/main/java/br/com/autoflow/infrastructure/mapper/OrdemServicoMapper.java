package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.HistoricoVeiculoResponse;
import br.com.autoflow.application.dto.MetricaOsResponse;
import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
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

        default HistoricoVeiculoResponse toHistoricoResponse(OrdemServico os) {
                if (os == null) {
                        return null;
                }
                return new HistoricoVeiculoResponse(
                        os.getIdOs(),
                        os.getStatusOS(),
                        os.getDsRelatoCliente(),
                        os.getDsDiagnostico(),
                        os.getNrKmEntrada(),
                        os.getDtAberturaOs(),
                        os.getDtEncerramentoOs(),
                        os.getServicosExecucao() != null ? os.getServicosExecucao().stream().map(se -> {
                                List<HistoricoVeiculoResponse.PecaHistorico> pecas = new java.util.ArrayList<>();
                                if (os.getIdsOrcamento() != null) {
                                        for (Orcamento orcamento : os.getIdsOrcamento()) {
                                                if (orcamento.getItens() != null) {
                                                        for (OrcamentoItem item : orcamento.getItens()) {
                                                                boolean pertenceAoServico = item.getOrcamentoServico() != null
                                                                        && item.getOrcamentoServico().getServico() != null
                                                                        && item.getOrcamentoServico().getServico().getIdServico().equals(se.getServico().getIdServico());
                                                                if (pertenceAoServico) {
                                                                        pecas.add(new HistoricoVeiculoResponse.PecaHistorico(
                                                                                item.getIdEstoque(),
                                                                                "Item de Estoque",
                                                                                item.getQuantidade(),
                                                                                item.getValorUnitario() != null ? item.getValorUnitario().doubleValue() : 0.0
                                                                        ));
                                                                }
                                                        }
                                                }
                                        }
                                }
                                return new HistoricoVeiculoResponse.ServicoHistorico(
                                        se.getServico().getIdServico(),
                                        se.getServico().getDsServico(),
                                        se.getServico().getVlServico() != null ? se.getServico().getVlServico().doubleValue() : 0.0,
                                        pecas
                                );
                        }).toList() : List.of()
                );
        }
}