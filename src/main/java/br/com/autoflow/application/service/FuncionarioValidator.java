package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FuncionarioValidator {

    private final FuncionarioRepository repository;

    public void validarParaCriar(FuncionarioRequest request) {
        validarIdadeMinima(request.dataNascimento());
        validarCpfUnico(request.cpf());
        validarEmailUnico(request.email());
    }
    public void validarParaAtualizar(UUID id, FuncionarioRequest request) {
        validarIdadeMinima(request.dataNascimento());
        validarCpfUnicoParaOutroFuncionario(id, request.cpf());
        validarEmailUnicoParaOutroFuncionario(id, request.email());
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
        if (repository.existsByCpf(cpf)) {
            throw new DadosJaCadastradosException("CPF já cadastrado: " + cpf);
        }
    }

    private void validarEmailUnico(String email) {
        if (repository.existsByEmail(email)) {
            throw new DadosJaCadastradosException("E-mail já cadastrado: " + email);
        }
    }
    private void validarCpfUnicoParaOutroFuncionario(UUID id, String cpf) {
        repository.findByCpf(cpf)
                .filter(funcionario -> !funcionario.getIdFuncionario().equals(id))
                .ifPresent(funcionario -> {
                    throw new DadosJaCadastradosException("CPF já cadastrado para outro funcionário: " + cpf);
                });
    }

    private void validarEmailUnicoParaOutroFuncionario(UUID id, String email) {
        repository.findByEmail(email)
                .filter(funcionario -> !funcionario.getIdFuncionario().equals(id))
                .ifPresent(funcionario -> {
                    throw new DadosJaCadastradosException("E-mail já cadastrado para outro funcionário: " + email);
                });
    }
}