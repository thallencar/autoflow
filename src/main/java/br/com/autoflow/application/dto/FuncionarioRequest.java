package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record FuncionarioRequest(

        @NotBlank(message = "O CPF é obrigatório.")
        @CPF(message = "O CPF informado é inválido.")
        String cpf,

        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O telefone é obrigatório.")
        String telefone,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado deve ter um formato válido.")
        String email,

        @NotNull(message = "O gênero é obrigatório.")
        Genero genero,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve ser uma data no passado.")
        LocalDate dataNascimento,

        @NotNull(message = "O cargo é obrigatório.")
        Cargo cargo,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoRequest endereco
) {}