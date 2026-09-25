package br.com.autoflow.application.validator;

import br.com.autoflow.domain.exception.DadosJaCadastradosException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FuncionarioValidator {

    private final FuncionarioRepositoryPort repositoryPort;

    public void validarParaCriar(Funcionario funcionario) {
        validarIdadeMinima(funcionario.getDataNascimento());
        validarCpfUnico(funcionario.getCpf());
        validarEmailUnico(funcionario.getEmail());
    }

    public void validarParaAtualizar(UUID id, Funcionario funcionario) {
        validarIdadeMinima(funcionario.getDataNascimento());
        validarCpfUnicoParaOutroFuncionario(id, funcionario.getCpf());
        validarEmailUnicoParaOutroFuncionario(id, funcionario.getEmail());
    }

    private void validarIdadeMinima(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new RegraNegocioException("A data de nascimento é obrigatória.");
        }
        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.now(java.time.ZoneId.systemDefault()));
        if (idade < 16) {
            throw new RegraNegocioException("O funcionário deve ter no mínimo 16 anos.");
        }
    }

    private void validarCpfUnico(String cpf) {
        if (repositoryPort.existsByCpf(cpf)) {
            throw new DadosJaCadastradosException("CPF já cadastrado: " + cpf);
        }
    }

    private void validarEmailUnico(String email) {
        if (repositoryPort.existsByEmail(email)) {
            throw new DadosJaCadastradosException("E-mail já cadastrado: " + email);
        }
    }

    private void validarCpfUnicoParaOutroFuncionario(UUID id, String cpf) {
        repositoryPort.findByCpf(cpf)
                .filter(f -> !f.getId().equals(id))
                .ifPresent(f -> {
                    throw new DadosJaCadastradosException("CPF já cadastrado para outro funcionário: " + cpf);
                });
    }

    private void validarEmailUnicoParaOutroFuncionario(UUID id, String email) {
        repositoryPort.findByEmail(email)
                .filter(f -> !f.getId().equals(id))
                .ifPresent(f -> {
                    throw new DadosJaCadastradosException("E-mail já cadastrado para outro funcionário: " + email);
                });
    }
}