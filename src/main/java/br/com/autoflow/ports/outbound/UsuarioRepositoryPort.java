package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findByLogin(String login);
    Optional<Usuario> findByFuncionario(Funcionario funcionario);
    Optional<Usuario> findByCliente(Cliente cliente);
    Optional<Usuario> findByFuncionarioId(UUID idFuncionario);
    Optional<Usuario> findById(UUID id);
    void delete(Usuario usuario);
    void flush();
}