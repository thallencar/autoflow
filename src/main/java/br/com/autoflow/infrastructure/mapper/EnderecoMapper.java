package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.EnderecoResponse;
import br.com.autoflow.domain.model.Endereco;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

    public Endereco toEntity(EnderecoRequest request) {

        return Endereco.builder()
                .logradouro(request.logradouro())
                .numero(request.numero())
                .complemento(request.complemento())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .uf(request.uf())
                .cep(request.cep())
                .build();
    }

    public EnderecoResponse toResponse(Endereco endereco) {

        return new EnderecoResponse(
                endereco.getId(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getCep()
        );
    }
}