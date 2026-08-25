package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    UserDetails findByLogin(String login);
    Optional<Usuario> findUsuarioByLogin(String login);
    Optional<Usuario> findByFuncionario (Funcionario funcionario);
    Optional<Usuario> findByCliente (Cliente cliente);
    Optional<Usuario> findByFuncionario_IdFuncionario(UUID idFuncionario);
}
