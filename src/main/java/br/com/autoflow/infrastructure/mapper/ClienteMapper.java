package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.ClienteResponse;
import br.com.autoflow.domain.model.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteMapper {

    private final EnderecoMapper enderecoMapper;

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
