package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.EstoqueEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataEstoqueRepository;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EstoqueRepositoryAdapter implements EstoqueRepositoryPort {

    private final SpringDataEstoqueRepository repository;
    private final EstoqueEntityMapper mapper;

    @Override
    public Estoque save(Estoque estoque) {
        var entity = mapper.toEntity(estoque);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Estoque> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Estoque> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Estoque> findByNomeItemContainingIgnoreCase(String nome) {
        return repository.findByNomeItemContainingIgnoreCase(nome).stream().map(mapper::toDomain).toList();
    }
}