package br.com.autoflow.ports.inbound.funcionario;

import br.com.autoflow.domain.model.Funcionario;

import java.util.List;

public interface ListarFuncionariosUseCase {
    List<Funcionario> listar();
}
