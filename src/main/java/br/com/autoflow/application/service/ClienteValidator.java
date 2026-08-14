package br.com.autoflow.application.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClienteValidator {
    private final ClienteRepository repository;

    public void validarParaCriar(ClienteRequest request) {
        validarIdadeMinima(request.dataNascimento());
        validarDocumentoUnico(request.documento());
        validarEmailUnico(request.email());
    }

    private void validarIdadeMinima(LocalDate dataNascimento) {
        if(dataNascimento == null) {
            throw new RegraNegocioException("A data de nascimento é obrigatória");
        }
        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.now());
        if (idade < 18) {
            throw new RegraNegocioException("O cliente deve ter no mínimo 18 anos.");
        }
    }

    private void validarDocumentoUnico(String documento) {
        if (repository.existsByDocumento(documento)) {
            throw new DadosJaCadastradosException("Documento já cadastrado: " + documento);
        }
    }

    private void validarEmailUnico(String email) {
        if (repository.existsByEmail(email)) {
            throw new DadosJaCadastradosException("E-mail já cadastrado: " + email);
        }
    }
    
}
