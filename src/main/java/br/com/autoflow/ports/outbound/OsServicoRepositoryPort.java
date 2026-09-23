package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.OsServico;
import java.util.Optional;
import java.util.UUID;

public interface OsServicoRepositoryPort {
    OsServico save(OsServico osServico);
    Optional<OsServico> findById(UUID id);
    boolean existsByServico_IdServico(UUID servicoId);
    void delete(OsServico osServico);
}