package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    boolean existsByDsServicoIgnoreCase(String dsServico);
    Optional<Servico> findByDsServicoIgnoreCase(String dsServico);
}