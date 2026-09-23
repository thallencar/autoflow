package br.com.autoflow.application.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.domain.exception.RegraNegocioException;
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