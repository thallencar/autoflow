package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class FuncionarioValidator {

    private final FuncionarioRepository repository;

    public void validarParaCriar(FuncionarioRequest request) {
        validarIdadeMinima(request.dataNascimento());
        validarCpfUnico(request.cpf());
        validarEmailUnico(request.email());
    }

    private void validarIdadeMinima(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new RegraNegocioException("A data de nascimento é obrigatória.");
        }
        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.now());
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
}