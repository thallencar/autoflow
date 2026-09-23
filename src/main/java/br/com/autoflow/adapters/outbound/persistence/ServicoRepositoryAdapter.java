package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.ServicoEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataServicoRepository;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.outbound.ServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServicoRepositoryAdapter implements ServicoRepositoryPort {

    private final SpringDataServicoRepository repository;
    private final ServicoEntityMapper mapper;

    @Override
    public Servico save(Servico servico) {
        var entity = mapper.toEntity(servico);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Page<Servico> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<Servico> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByDsServicoIgnoreCase(String dsServico) {
        return repository.existsByDsServicoIgnoreCase(dsServico);
    }

    @Override
    public Optional<Servico> findByDsServicoIgnoreCase(String dsServico) {
        return repository.findByDsServicoIgnoreCase(dsServico).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}