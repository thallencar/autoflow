package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.entity.EnderecoEntity;
import br.com.autoflow.adapters.outbound.persistence.mapper.EnderecoEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataEnderecoRepository;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnderecoRepositoryAdapter implements EnderecoRepositoryPort {

    private final SpringDataEnderecoRepository repository;
    private final EnderecoEntityMapper mapper;

    @Override
    public Endereco save(Endereco endereco) {
        EnderecoEntity entity = mapper.toEntity(endereco);
        EnderecoEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Endereco> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Endereco> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void delete(Endereco endereco) {
        repository.delete(mapper.toEntity(endereco));
    }

    @Override
    public Optional<Endereco> findByCepAndNumero(String cep, Integer numero) {
        return repository.findByCepAndNumero(cep, numero)
                .map(mapper::toDomain);
    }
}