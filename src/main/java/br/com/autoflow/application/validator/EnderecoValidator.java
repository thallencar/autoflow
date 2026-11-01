package br.com.autoflow.application.validator;

import java.util.Set;
import org.springframework.stereotype.Component;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.exception.RegraNegocioException;

@Component
public class EnderecoValidator {

    private final Set<String> ufsValidas = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    );

    public void validarUf(Endereco endereco) {
        if (endereco == null || endereco.getUf() == null || !ufsValidas.contains(endereco.getUf().toUpperCase())) {
            throw new RegraNegocioException("UF inválida: " + (endereco == null ? null : endereco.getUf()));
        }
    }

    public void validarUf(EnderecoRequest request) {
        String uf = request == null ? null : request.uf();
        if (uf == null || !ufsValidas.contains(uf.toUpperCase())) {
            throw new RegraNegocioException("UF inválida: " + uf);
        }
    }
}