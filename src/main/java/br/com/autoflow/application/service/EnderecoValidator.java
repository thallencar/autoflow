package br.com.autoflow.application.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnderecoValidator {
    private final Set<String> ufsValidas = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    );

    public void validarUf(EnderecoRequest request) {
        if (request.uf() == null || !ufsValidas.contains(request.uf().toUpperCase())) {
            throw new RegraNegocioException("UF inválida: " + request.uf());
        }
    }
}