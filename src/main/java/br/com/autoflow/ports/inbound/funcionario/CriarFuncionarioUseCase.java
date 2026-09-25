package br.com.autoflow.ports.inbound.funcionario;

import br.com.autoflow.domain.model.Funcionario;

public interface CriarFuncionarioUseCase {
    Funcionario criar(Funcionario funcionario);
}
