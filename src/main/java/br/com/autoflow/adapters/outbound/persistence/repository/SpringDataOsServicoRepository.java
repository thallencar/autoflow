package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.OsServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataOsServicoRepository extends JpaRepository<OsServicoEntity, UUID> {
    boolean existsByServico_IdServico(UUID servicoId);
}