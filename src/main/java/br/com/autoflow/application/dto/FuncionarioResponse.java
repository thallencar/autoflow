package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.model.Funcionario;

import java.time.LocalDate;
import java.util.UUID;

public record FuncionarioResponse(
        UUID id,
        String cpf,
        String nome,
        String telefone,
        String email,
        Genero genero,
        LocalDate dataNascimento,
        String cargo,
        UUID idEndereco
) {
    // Método utilitário de fábrica (Factory Method) para converter do Domínio para o Response DTO
    public static FuncionarioResponse fromDomain(Funcionario funcionario) {
        UUID enderecoId = (funcionario.getEndereco() != null) ? funcionario.getEndereco().getId() : null;

        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getCpf(),
                funcionario.getNome(),
                funcionario.getTelefone(),
                funcionario.getEmail(),
                funcionario.getGenero(),
                funcionario.getDataNascimento(),
                funcionario.getCargo(),
                enderecoId
        );
    }
}
