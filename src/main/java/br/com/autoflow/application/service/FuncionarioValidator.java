package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.exception.CpfJaCadastradoException;
import br.com.autoflow.exception.EmailJaCadastradoException;
import br.com.autoflow.exception.FuncionarioMenorDeIdadeException;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class FuncionarioValidator {

    private final FuncionarioRepository repository;

    public FuncionarioValidator(FuncionarioRepository repository) {
        this.repository = repository;
    }

    public void validarParaCriar(FuncionarioRequest request) {
        validarIdadeMinima(request.dataNascimento());
        validarCpfUnico(request.cpf());
        validarEmailUnico(request.email());
    }

    private void validarIdadeMinima(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new FuncionarioMenorDeIdadeException("A data de nascimento é obrigatória.");
        }

        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.now());

        if (idade < 16) {
            throw new FuncionarioMenorDeIdadeException("O funcionário deve ter no mínimo 16 anos.");
        }
    }

    private void validarCpfUnico(String cpf) {
        if (repository.existsByCpf(cpf)) {
            throw new CpfJaCadastradoException(cpf);
        }
    }

    private void validarEmailUnico(String email) {
        if (repository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
    }
}