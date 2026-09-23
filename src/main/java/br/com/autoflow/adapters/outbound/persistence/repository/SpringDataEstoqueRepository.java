package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.EstoqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataEstoqueRepository extends JpaRepository<EstoqueEntity, UUID> {
    List<EstoqueEntity> findByNomeItemContainingIgnoreCase(String nome);
}