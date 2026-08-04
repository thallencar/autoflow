package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Funcionario;

import java.util.Optional;
import java.util.UUID;

public interface FuncionarioRepository {
    Funcionario salvar(Funcionario funcionario);
    Optional<Funcionario> buscarPorId(UUID id);
    Optional<Funcionario> buscarPorCpf(String cpf);
}
