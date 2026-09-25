package br.com.autoflow.ports.inbound.funcionario;

import br.com.autoflow.domain.model.Funcionario;

public interface BuscarFuncionarioPorCpfUseCase {
    Funcionario buscarPorCpf(String cpf);
}
