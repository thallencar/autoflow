package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataOrcamentoRepository extends JpaRepository<OrcamentoEntity, UUID> {
    boolean existsByIdAndOrdemServicoIsNotNull(UUID idOrcamento);
    List<OrcamentoEntity> findByOrdemServicoIdOs(UUID idOs);

    @Modifying
    @Query(value = "DELETE FROM tb_orcamento_itens WHERE id_orcamento = :id", nativeQuery = true)
    void deletarItensDiretosPorOrcamento(@Param("id") UUID id);

    @Modifying
    @Query(value = "DELETE FROM tb_orcamento_itens WHERE id_orcamento_servicos IN (SELECT id_orcamento_servicos FROM tb_orcamento_servicos WHERE id_orcamento = :id)", nativeQuery = true)
    void deletarItensPorServicosDoOrcamento(@Param("id") UUID id);

    @Modifying
    @Query(value = "DELETE FROM tb_orcamento_servicos WHERE id_orcamento = :id", nativeQuery = true)
    void deletarServicosPorOrcamento(@Param("id") UUID id);
}