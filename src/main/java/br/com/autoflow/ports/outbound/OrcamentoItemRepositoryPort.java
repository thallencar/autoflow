package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.OrcamentoItem;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoItemRepositoryPort {
    OrcamentoItem save(OrcamentoItem orcamentoItem);
    Optional<OrcamentoItem> findById(UUID id);
    void delete(OrcamentoItem orcamentoItem);
}