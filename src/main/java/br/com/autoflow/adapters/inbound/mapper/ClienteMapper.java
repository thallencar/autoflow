package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endereco", source = "endereco")
    Cliente toDomain(ClienteRequest request, Endereco endereco);

    // Novo mapeamento para a atualização vinda do Controller
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "dataNascimento", ignore = true)
    @Mapping(target = "endereco", source = "endereco")
    Cliente toDomain(ClienteUpdateRequest request, Endereco endereco);

    @Mapping(target = "endereco", source = "endereco")
    ClienteResponse toResponse(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    Endereco toEnderecoDomain(EnderecoRequest request);
}