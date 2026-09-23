package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.OsServicoEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataOsServicoRepository;
import br.com.autoflow.domain.model.OsServico;
import br.com.autoflow.ports.outbound.OsServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OsServicoRepositoryAdapter implements OsServicoRepositoryPort {

    private final SpringDataOsServicoRepository repository;
    private final OsServicoEntityMapper mapper;

    @Override
    public OsServico save(OsServico osServico) {
        var entity = mapper.toEntity(osServico);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OsServico> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByServico_IdServico(UUID servicoId) {
        return repository.existsByServico_IdServico(servicoId);
    }

    @Override
    public void delete(OsServico osServico) {
        repository.delete(mapper.toEntity(osServico));
    }
}