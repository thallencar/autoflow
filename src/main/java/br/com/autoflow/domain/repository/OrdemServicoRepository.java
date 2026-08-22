package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.enums.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {
    boolean existsByIdVeiculoAndStatusOSNotIn(UUID veiculoId, Collection<StatusOS> status);
    long countByStatusOSNot(StatusOS statusOs);
    Optional<OrdemServico> findTopByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo);
    boolean existsByIdsOrcamentoContaining(List<Orcamento> idsOrcamento);
}