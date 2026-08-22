package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {
    boolean existsByIdAndOrdemServicoIsNotNull(UUID idOrcamento);
    List<Orcamento> findByOrdemServicoIdOs(UUID idOs);

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