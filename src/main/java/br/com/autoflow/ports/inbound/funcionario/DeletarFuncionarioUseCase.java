package br.com.autoflow.ports.inbound.funcionario;

import java.util.UUID;

public interface DeletarFuncionarioUseCase {
    void deletar(UUID id);
}
