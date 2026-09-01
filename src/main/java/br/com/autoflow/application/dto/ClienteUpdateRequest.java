package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record ClienteUpdateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado deve ter um formato válido.")
        String email,

        @NotBlank(message = "O telefone é obrigatório.")
        String telefone,

        Genero genero,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoRequest endereco
) {}