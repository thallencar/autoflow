package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.FuncionarioEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataFuncionarioRepository;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FuncionarioRepositoryAdapter implements FuncionarioRepositoryPort {

    private final SpringDataFuncionarioRepository repository;
    private final FuncionarioEntityMapper mapper;

    @Override
    public Funcionario save(Funcionario funcionario) {
        var entity = mapper.toEntity(funcionario);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Funcionario> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Funcionario> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Funcionario> findByCpf(String cpf) {
        return repository.findByCpf(cpf).map(mapper::toDomain);
    }

    @Override
    public Optional<Funcionario> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return repository.existsByCpf(cpf);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void delete(Funcionario funcionario) {
        repository.delete(mapper.toEntity(funcionario));
    }
}