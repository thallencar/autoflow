package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.application.dto.ClienteResponse;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {EnderecoMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endereco", source = "endereco")
    Cliente toEntity(ClienteRequest request, Endereco endereco);

    @Mapping(target = "endereco", source = "endereco")
    ClienteResponse toResponse(Cliente cliente);
}