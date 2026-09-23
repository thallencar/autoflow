package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.UsuarioEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataUsuarioRepository;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;
    private final UsuarioEntityMapper mapper;

    @Override
    public Usuario save(Usuario usuario) {
        var entity = mapper.toEntity(usuario);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Usuario> findByLogin(String login) {
        return repository.findByLogin(login).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByFuncionario(Funcionario funcionario) {
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByCliente(Cliente cliente) {
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByFuncionarioId(UUID idFuncionario) {
        return repository.findByFuncionario_Id(idFuncionario).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void delete(Usuario usuario) {
        if (usuario != null) {
            var entity = mapper.toEntity(usuario);
            repository.delete(entity);
        }
    }

    @Override
    public void flush() {
        repository.flush();
    }
}