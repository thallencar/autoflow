package com.autoflow.domain.repository;

import com.autoflow.domain.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, UUID> {

     List<Estoque> findByNomeItemContainingIgnoreCase(String nome);
}