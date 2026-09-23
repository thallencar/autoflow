package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.OrcamentoServico;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoServicoRepositoryPort {
    OrcamentoServico save(OrcamentoServico orcamentoServico);
    Optional<OrcamentoServico> findById(UUID id);
    boolean existsByServico_IdServico(UUID idServico);
    void delete(OrcamentoServico orcamentoServico);
}