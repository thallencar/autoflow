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
        validarDocumento(request.documento());
        validarEmailUnico(request.email());
    }

    private void validarIdadeMinima(LocalDate dataNascimento) {
        if(dataNascimento == null) {
            throw new RegraNegocioException("A data de nascimento é obrigatória");
        }
        long idade = ChronoUnit.YEARS.between(dataNascimento, LocalDate.now(java.time.ZoneId.systemDefault()));
        if (idade < 18) {
            throw new RegraNegocioException("O cliente deve ter no mínimo 18 anos.");
        }
    }

    private void validarEmailUnico(String email) {
        if (repository.existsByEmail(email)) {
            throw new DadosJaCadastradosException("E-mail já cadastrado: " + email);
        }
    }

    private void validarDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new RegraNegocioException("O documento (CPF/CNPJ) é obrigatório.");
        }
        String documentoLimpo = documento.replaceAll("\\D", "");

        if (documentoLimpo.length() == 11) {
            if (!isCpfValido(documentoLimpo)) {
                throw new RegraNegocioException("CPF inválido: " + documento);
            }
        } else if (documentoLimpo.length() == 14) {
            if (!isCnpjValido(documentoLimpo)) {
                throw new RegraNegocioException("CNPJ inválido: " + documento);
            }
        } else {
            throw new RegraNegocioException("O documento deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos) válido.");
        }
        if (repository.existsByDocumento(documentoLimpo) || repository.existsByDocumento(documento)) {
            throw new DadosJaCadastradosException("Documento já cadastrado: " + documento);
        }
    }
    private boolean isCpfValido(String cpf) {
        if (cpf.matches("(\\d)\\1{10}")) return false; // Evita CPFs com todos os números iguais (ex: 111.111.111-11)

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int digito1 = 11 - (soma % 11);
            if (digito1 > 9) digito1 = 0;

            if (digito1 != (cpf.charAt(9) - '0')) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int digito2 = 11 - (soma % 11);
            if (digito2 > 9) digito2 = 0;

            return digito2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCnpjValido(String cnpj) {
        if (cnpj.matches("(\\d)\\1{13}")) return false; // Evita CNPJs com todos os números iguais

        try {
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += (cnpj.charAt(i) - '0') * peso1[i];
            }
            int digito1 = soma % 11;
            digito1 = digito1 < 2 ? 0 : 11 - digito1;

            if (digito1 != (cnpj.charAt(12) - '0')) return false;

            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += (cnpj.charAt(i) - '0') * peso2[i];
            }
            int digito2 = soma % 11;
            digito2 = digito2 < 2 ? 0 : 11 - digito2;

            return digito2 == (cnpj.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
