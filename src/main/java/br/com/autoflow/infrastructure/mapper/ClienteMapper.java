package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.application.dto.ClienteResponse;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteMapper {

    private final EnderecoMapper enderecoMapper;

    public Cliente toEntity(ClienteRequest request, Endereco endereco) {
        return Cliente.builder()
                .documento(request.documento())
                .nome(request.nome())
                .telefone(request.telefone())
                .email(request.email())
                .genero(request.genero())
                .dataNascimento(request.dataNascimento())
                .endereco(endereco)
                .build();
    }

    public ClienteResponse toResponse(Cliente cliente) {
                return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getDataNascimento(),
                cliente.getTelefone(),
                cliente.getGenero(),
                enderecoMapper.toResponse(cliente.getEndereco())
        );
    }
}
