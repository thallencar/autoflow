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
    @Query("SELECT o FROM OrdemServicoEntity o WHERE (:status IS NULL OR o.statusOS = :status) AND (:dataInicio IS NULL OR o.dtAberturaOs >= :dataInicio) AND (:dataFim IS NULL OR o.dtAberturaOs <= :dataFim)")
    Page<OrdemServicoEntity> findMetricasComFiltro(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim, @Param("status") StatusOS status, Pageable pageable);
}