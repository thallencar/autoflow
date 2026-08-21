package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.OsServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OsServicoRepository extends JpaRepository<OsServico, UUID> {
   boolean existsByServico_IdServico(UUID servicoId);
}