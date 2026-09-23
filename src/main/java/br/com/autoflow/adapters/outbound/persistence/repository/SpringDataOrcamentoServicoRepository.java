package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataOrcamentoServicoRepository extends JpaRepository<OrcamentoServicoEntity, UUID> {
    boolean existsByServico_IdServico(UUID idServico);
}