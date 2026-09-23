package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Funcionario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioRepositoryPort {
    Funcionario save(Funcionario funcionario);
    List<Funcionario> findAll();
    Optional<Funcionario> findById(UUID id);
    Optional<Funcionario> findByCpf(String cpf);
    Optional<Funcionario> findByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
    void delete(Funcionario funcionario);
}