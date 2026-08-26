package br.com.autoflow.application.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import br.com.autoflow.application.dto.EnderecoRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnderecoValidator {
    private final Set<String> ufsValidas = Set.of(
        "AC",
        "AL",
        "AP",
        "AM",
        "BA",
        "CE",
        "DF",
        "ES",
        "GO",
        "MA",
        "MT",
        "MS",
        "MG",
        "PA",
        "PB",
        "PR",
        "PE",
        "PI",
        "RJ",
        "RN",
        "RS",
        "RO",
        "RR",
        "SC",
        "SP",
        "SE",
        "TO"
    );

    public void validarUf(EnderecoRequest request) {
        if(ufsValidas.contains(request.uf()));
    }
}
