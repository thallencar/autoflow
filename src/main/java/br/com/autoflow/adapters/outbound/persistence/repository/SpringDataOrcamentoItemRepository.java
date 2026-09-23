package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.OrcamentoItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataOrcamentoItemRepository extends JpaRepository<OrcamentoItemEntity, UUID> {
}