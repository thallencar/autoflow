package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.Genero;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record FuncionarioRequest(
        @NotBlank String cpf,
        @NotBlank String nome,
        @NotBlank String telefone,
        @Email @NotBlank String email,
        @NotNull Genero genero,
        @NotNull LocalDate dataNascimento,
        @NotBlank String cargo,
        @NotNull UUID idEndereco
) {}
