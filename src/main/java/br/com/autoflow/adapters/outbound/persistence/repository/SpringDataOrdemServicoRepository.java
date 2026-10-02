package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.domain.enums.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrdemServicoRepository extends JpaRepository<OrdemServicoEntity, UUID> {
    Page<OrdemServicoEntity> findByStatusOS(StatusOS status, Pageable pageable);

    Long countByStatusOSNotIn(List<StatusOS> status);

    boolean existsByIdVeiculoAndStatusOSNotIn(UUID idVeiculo, List<StatusOS> status);

    Optional<OrdemServicoEntity> findTopByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo);

    Page<OrdemServicoEntity> findByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo, Pageable pageable);

    boolean existsByIdVeiculo(UUID idVeiculo);

    @Query("SELECT o FROM OrdemServicoEntity o WHERE " +
            "(:status IS NULL OR o.statusOS = :status) AND " +
            "(cast(:inicio as timestamp) IS NULL OR o.dtAberturaOs >= :inicio) AND " +
            "(cast(:fim as timestamp) IS NULL OR o.dtAberturaOs <= :fim)")
    Page<OrdemServicoEntity> findMetricasComFiltro(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("status") StatusOS status,
            Pageable pageable
    );

    @Query("SELECT o FROM OrdemServicoEntity o WHERE o.statusOS NOT IN :statusExcluidos " +
            "ORDER BY " +
            "CASE o.statusOS " +
            "  WHEN 'EM_EXECUCAO' THEN 1 " +
            "  WHEN 'AGUARDANDO_APROVACAO' THEN 2 " +
            "  WHEN 'ORCAMENTO_APROVADO' THEN 3 " +
            "  WHEN 'EM_DIAGNOSTICO' THEN 4 " +
            "  WHEN 'RECEBIDA' THEN 5 " +
            "  ELSE 6 END ASC, " +
            "o.dtAberturaOs ASC")
    Page<OrdemServicoEntity> findByStatusOSNotIn(
            @Param("statusExcluidos") List<StatusOS> statusExcluidos,
            Pageable pageable
    );
}