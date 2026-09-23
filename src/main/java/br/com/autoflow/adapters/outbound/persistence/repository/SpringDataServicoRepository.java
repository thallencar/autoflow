package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataServicoRepository extends JpaRepository<ServicoEntity, UUID> {
    boolean existsByDsServicoIgnoreCase(String dsServico);
    Optional<ServicoEntity> findByDsServicoIgnoreCase(String dsServico);
}