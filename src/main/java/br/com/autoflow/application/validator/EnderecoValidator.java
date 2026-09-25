package br.com.autoflow.application.validator;

import java.util.Set;
import org.springframework.stereotype.Component;
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
        if (endereco.getUf() == null || !ufsValidas.contains(endereco.getUf().toUpperCase())) {
            throw new RegraNegocioException("UF inválida: " + endereco.getUf());
        }
    }
}