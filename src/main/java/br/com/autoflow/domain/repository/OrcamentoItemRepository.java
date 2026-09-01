package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrcamentoItemRepository extends JpaRepository<OrcamentoItem, UUID> {
}