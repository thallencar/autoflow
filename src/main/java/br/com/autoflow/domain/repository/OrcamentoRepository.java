package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {
    boolean existsByIdAndOrdemServicoIsNotNull(UUID idOrcamento);
    List<Orcamento> findByOrdemServicoIdOs(UUID idOs);
}