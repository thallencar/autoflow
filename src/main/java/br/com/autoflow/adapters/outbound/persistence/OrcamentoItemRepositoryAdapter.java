package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.OrcamentoItemEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataOrcamentoItemRepository;
import br.com.autoflow.domain.model.OrcamentoItem;
import br.com.autoflow.ports.outbound.OrcamentoItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrcamentoItemRepositoryAdapter implements OrcamentoItemRepositoryPort {

    private final SpringDataOrcamentoItemRepository repository;
    private final OrcamentoItemEntityMapper mapper;

    @Override
    public OrcamentoItem save(OrcamentoItem orcamentoItem) {
        var entity = mapper.toEntity(orcamentoItem);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OrcamentoItem> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void delete(OrcamentoItem orcamentoItem) {
        repository.delete(mapper.toEntity(orcamentoItem));
    }
}