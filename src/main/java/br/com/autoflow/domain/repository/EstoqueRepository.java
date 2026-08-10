package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, UUID> {

     List<Estoque> findByNomeItemContainingIgnoreCase(String nome);
}