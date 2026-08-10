package com.projeto.repository;

import com.projeto.model.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrcamentoItemRepository extends JpaRepository<OrcamentoItem, UUID> {
    List<OrcamentoItem> findByOrcamentoId(UUID idOrcamento);
}