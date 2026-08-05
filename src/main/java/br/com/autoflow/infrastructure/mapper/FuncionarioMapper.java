package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Funcionario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FuncionarioMapper {

    private final EnderecoMapper enderecoMapper;

    public Funcionario toEntity(FuncionarioRequest request, Endereco endereco) {

        return Funcionario.builder()
                .cpf(request.cpf())
                .nome(request.nome())
                .telefone(request.telefone())
                .email(request.email())
                .genero(request.genero())
                .cargo(request.cargo())
                .dataNascimento(request.dataNascimento())
                .endereco(endereco)
                .build();
    }

    public FuncionarioResponse toResponse(Funcionario funcionario) {

        return new FuncionarioResponse(
                funcionario.getIdFuncionario(),
                funcionario.getCpf(),
                funcionario.getNome(),
                funcionario.getTelefone(),
                funcionario.getEmail(),
                funcionario.getGenero(),
                funcionario.getDataNascimento(),
                funcionario.getCargo(),
                enderecoMapper.toResponse(
                        funcionario.getEndereco()
                )
        );
    }
}