package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "O documento (CPF/CNPJ) é obrigatório.")
        @Size(min = 11, max = 14, message = "O documento deve ter entre 11 (CPF) e 14 (CNPJ) dígitos.")
        String documento,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado deve ter um formato válido.")
        String email,

        @Past(message = "A data de nascimento deve ser uma data no passado.")
        LocalDate dataNascimento,

        @NotBlank(message = "O telefone é obrigatório.")
        String telefone,

        Genero genero,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoRequest endereco
) {
}
