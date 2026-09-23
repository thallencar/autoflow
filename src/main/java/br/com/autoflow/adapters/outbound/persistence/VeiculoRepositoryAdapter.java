package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.VeiculoEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataVeiculoRepository;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VeiculoRepositoryAdapter implements VeiculoRepositoryPort {

    private final SpringDataVeiculoRepository repository;
    private final VeiculoEntityMapper mapper;

    @Override
    public Veiculo save(Veiculo veiculo) {
        var entity = mapper.toEntity(veiculo);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Veiculo> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Veiculo> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Veiculo> findByPlaca(String placa) {
        return repository.findByPlaca(placa).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return repository.existsByPlaca(placa);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void delete(Veiculo veiculo) {
        if (veiculo != null) {
            var entity = mapper.toEntity(veiculo);
            repository.delete(entity);
        }
    }

    @Override
    public boolean existsByClienteId(UUID clienteId) {
        return repository.existsByClienteId(clienteId);
    }

    @Override
    public boolean existsByIdAndClienteId(UUID id, UUID clienteId) {
        return repository.findByIdAndClienteId(id, clienteId).isPresent();
    }
}