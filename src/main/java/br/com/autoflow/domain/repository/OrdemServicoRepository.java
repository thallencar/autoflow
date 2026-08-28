package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.enums.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    boolean existsByIdVeiculoAndStatusOSNotIn(UUID veiculoId, Collection<StatusOS> status);

    long countByStatusOSNotIn(Collection<StatusOS> statusList);

    Optional<OrdemServico> findTopByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo);

    Page<OrdemServico> findByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo, Pageable pageable);

    Page<OrdemServico> findByStatusOS(StatusOS statusOS, Pageable pageable);

    boolean existsByIdVeiculo(UUID veiculoId);

    @Query("""
        SELECT os FROM OrdemServico os
        WHERE (:status IS NULL OR os.statusOS = :status)
          AND (cast(:dataInicio as timestamp) IS NULL OR os.dataInicioExecucao >= :dataInicio)
          AND (cast(:dataFim as timestamp) IS NULL OR os.dataFimExecucao <= :dataFim)
        ORDER BY os.dataInicioExecucao DESC
    """)
    Page<OrdemServico> findMetricasComFiltro(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("status") StatusOS status,
            Pageable pageable
    );
}