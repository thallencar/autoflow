package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;

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
        Cargo cargo,
        boolean ocupado,
        EnderecoResponse endereco
) {}
