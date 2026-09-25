package br.com.autoflow.ports.inbound.funcionario;

import br.com.autoflow.domain.model.Funcionario;

import java.util.UUID;

public interface AtualizarFuncionarioUseCase {
    Funcionario atualizar(UUID id, Funcionario funcionario);
}
