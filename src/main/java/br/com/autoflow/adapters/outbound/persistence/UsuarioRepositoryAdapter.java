package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.autoflow.adapters.outbound.persistence.mapper.UsuarioEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataFuncionarioRepository;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataUsuarioRepository;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;
    private final UsuarioEntityMapper mapper;
    private final SpringDataFuncionarioRepository funcionarioRepository;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);

        // Se o usuário possui um funcionário associado com ID, buscamos a instância gerenciada
        // para evitar que o Hibernate tente persistir entidades destacadas (detached) em cascata.
        if (entity.getFuncionario() != null && entity.getFuncionario().getIdFuncionario() != null) {
            var funcionarioGerenciado = funcionarioRepository.findById(entity.getFuncionario().getIdFuncionario())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            // Se o funcionário possui um endereço com ID preenchido, garantimos que ele também seja gerenciado
            if (funcionarioGerenciado.getEndereco() != null && entity.getFuncionario().getEndereco() != null
                    && entity.getFuncionario().getEndereco().getId() != null) {
                entity.getFuncionario().getEndereco().setId(funcionarioGerenciado.getEndereco().getId());
            }

            entity.setFuncionario(funcionarioGerenciado);
        }

        entity = repository.save(entity);
        return mapper.toDomain(entity);
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