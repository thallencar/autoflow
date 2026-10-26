package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoItemEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoServicoEntity;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
import br.com.autoflow.domain.model.OrcamentoServico;

import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {
        OrdemServicoEntityMapper.class,
        OrcamentoServicoEntityMapper.class,
        OrcamentoItemEntityMapper.class
    },
    builder = @Builder(disableBuilder = true)
)
public interface OrcamentoEntityMapper {

    OrcamentoEntity toEntity(Orcamento orcamento);

    Orcamento toDomain(OrcamentoEntity entity);

    @AfterMapping
    default void vincularEntidades(@MappingTarget OrcamentoEntity orcamento) {

        if (orcamento.getServicos() != null) {
            for (OrcamentoServicoEntity servico : orcamento.getServicos()) {
                servico.setOrcamento(orcamento);

                if (servico.getItens() != null) {
                    for (OrcamentoItemEntity item : servico.getItens()) {
                        item.setOrcamentoServico(servico);
                        item.setOrcamento(orcamento);
                    }
                }
            }
        }

        if (orcamento.getItens() != null) {
            for (OrcamentoItemEntity item : orcamento.getItens()) {
                item.setOrcamento(orcamento);
            }
        }
    }

    @AfterMapping
    default void vincularDominio(@MappingTarget Orcamento orcamento) {

        if (orcamento.getServicos() != null) {
            for (OrcamentoServico servico : orcamento.getServicos()) {
                servico.setOrcamento(orcamento);

                if (servico.getItens() != null) {
                    for (OrcamentoItem item : servico.getItens()) {
                        item.setOrcamentoServico(servico);
                        item.setOrcamento(orcamento);
                    }
                }
            }
        }

        if (orcamento.getItens() != null) {
            for (OrcamentoItem item : orcamento.getItens()) {
                item.setOrcamento(orcamento);
            }
        }
    }
}