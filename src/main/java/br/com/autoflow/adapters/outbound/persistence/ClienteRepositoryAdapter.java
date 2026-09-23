package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.ClienteEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataClienteRepository;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final SpringDataClienteRepository repository;
    private final ClienteEntityMapper mapper;

    @Override
    public Cliente save(Cliente cliente) {
        var entity = mapper.toEntity(cliente);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Cliente> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByDocumento(String documento) {
        return repository.findByDocumento(documento).map(mapper::toDomain);
    }

    @Override
    public boolean existsByDocumento(String documento) {
        return repository.existsByDocumento(documento);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public List<Cliente> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(Cliente cliente) {
        repository.delete(mapper.toEntity(cliente));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}