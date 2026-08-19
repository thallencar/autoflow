package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.OrcamentoServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrcamentoServicoRepository extends JpaRepository<OrcamentoServico, UUID> {
    boolean existsByServico_IdServico(UUID idServico);
}