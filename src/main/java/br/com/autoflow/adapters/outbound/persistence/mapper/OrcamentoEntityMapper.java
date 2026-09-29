package br.com.autoflow.adapters.outbound.persistence.mapper;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoItemEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoServicoEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.adapters.outbound.persistence.entity.ServicoEntity;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrcamentoItem;
import br.com.autoflow.domain.model.OrcamentoServico;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.model.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrcamentoEntityMapper {

    default OrcamentoEntity toEntity(Orcamento orcamento) {
        if (orcamento == null) {
            return null;
        }

        OrcamentoEntity entity = OrcamentoEntity.builder()
                .id(orcamento.getId())
                .tipoOrcamento(orcamento.getTipoOrcamento())
                .status(orcamento.getStatus())
                .dataCriacao(orcamento.getDataCriacao())
                .dataExpiracao(orcamento.getDataExpiracao())
                .dataDecisao(orcamento.getDataDecisao())
                .subtotalPecas(orcamento.getSubtotalPecas())
                .maoObra(orcamento.getMaoObra())
                .total(orcamento.getTotal())
                .build();

        if (orcamento.getOrdemServico() != null) {
            OrdemServicoEntity osEntity = new OrdemServicoEntity();
            osEntity.setIdOs(orcamento.getOrdemServico().getIdOs());
            entity.setOrdemServico(osEntity);
        }

        if (orcamento.getServicos() != null) {
            List<OrcamentoServicoEntity> servicosEntities = new ArrayList<>();
            List<OrcamentoItemEntity> todosItensEntities = new ArrayList<>();

            for (OrcamentoServico servicoDomain : orcamento.getServicos()) {
                OrcamentoServicoEntity servicoEntity = new OrcamentoServicoEntity();
                servicoEntity.setMaoDeObra(servicoDomain.getMaoDeObra());
                servicoEntity.setOrcamento(entity);

                UUID idServicoExtraido = null;
                if (servicoDomain.getServico() != null) {
                    idServicoExtraido = servicoDomain.getServico().getIdServico();
                }

                if (idServicoExtraido != null) {
                    ServicoEntity servicoRef = new ServicoEntity();
                    servicoRef.setIdServico(idServicoExtraido);
                    servicoEntity.setServico(servicoRef);
                } else {
                    throw new IllegalStateException("O ID do serviço não pode ser nulo para o orçamento de serviços.");
                }

                if (servicoDomain.getItens() != null) {
                    List<OrcamentoItemEntity> itensEntities = new ArrayList<>();
                    for (OrcamentoItem itemDomain : servicoDomain.getItens()) {
                        OrcamentoItemEntity itemEntity = OrcamentoItemEntity.builder()
                                .id(itemDomain.getId())
                                .statusReserva(itemDomain.getStatusReserva())
                                .quantidade(itemDomain.getQuantidade())
                                .valorUnitario(itemDomain.getValorUnitario())
                                .valorTotal(itemDomain.getValorTotal())
                                .idEstoque(itemDomain.getIdEstoque())
                                .orcamento(entity)
                                .orcamentoServico(servicoEntity)
                                .build();

                        itensEntities.add(itemEntity);
                        todosItensEntities.add(itemEntity);
                    }
                    servicoEntity.setItens(itensEntities);
                }

                servicosEntities.add(servicoEntity);
            }

            entity.setServicos(servicosEntities);
            entity.setItens(todosItensEntities);
        }

        return entity;
    }

    default Orcamento toDomain(OrcamentoEntity entity) {
        if (entity == null) {
            return null;
        }

        OrdemServico ordemServico = null;
        if (entity.getOrdemServico() != null) {
            ordemServico = new OrdemServico();
            ordemServico.setIdOs(entity.getOrdemServico().getIdOs());
        }

        // Mapeia os serviços da entidade de volta para o modelo de domínio
        List<OrcamentoServico> servicosDomain = new ArrayList<>();
        if (entity.getServicos() != null) {
            for (OrcamentoServicoEntity servicoEntity : entity.getServicos()) {
                OrcamentoServico servicoDomain = new OrcamentoServico();
                // NOTA: Não definimos o ID do OrcamentoServico aqui se ele não deve aparecer no JSON de serviços.
                servicoDomain.setMaoDeObra(servicoEntity.getMaoDeObra());

                if (servicoEntity.getServico() != null) {
                    Servico servicoRef = new Servico();
                    servicoRef.setIdServico(servicoEntity.getServico().getIdServico());
                    servicoDomain.setServico(servicoRef);
                }

                // Mapeia os itens do serviço de volta para o domínio
                List<OrcamentoItem> itensDomain = new ArrayList<>();
                if (servicoEntity.getItens() != null) {
                    for (OrcamentoItemEntity itemEntity : servicoEntity.getItens()) {
                        OrcamentoItem itemDomain = new OrcamentoItem(
                                null,
                                itemEntity.getStatusReserva(),
                                itemEntity.getQuantidade(),
                                itemEntity.getValorUnitario(),
                                itemEntity.getIdEstoque()

                        );
                        itensDomain.add(itemDomain);
                    }
                }
                servicoDomain.setItens(itensDomain);
                servicosDomain.add(servicoDomain);
            }
        }

        return new Orcamento(
                entity.getId(),
                entity.getTipoOrcamento(),
                entity.getStatus(),
                entity.getDataCriacao(),
                entity.getDataExpiracao(),
                entity.getDataDecisao(),
                entity.getSubtotalPecas(),
                entity.getMaoObra(),
                entity.getTotal(),
                ordemServico,
                servicosDomain,
                new ArrayList<>()
        );
    }
}