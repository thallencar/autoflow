package br.com.autoflow.adapters.inbound.controller.dto;

import br.com.autoflow.domain.enums.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClienteUpdateRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        String telefone,

        @NotNull(message = "O gênero é obrigatório")
        Genero genero,

        @Valid
        EnderecoRequest endereco
) {}