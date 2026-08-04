package br.com.autoflow.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(

        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "O CEP deve ter o formato 00000-000.")
        String cep,

        @NotBlank(message = "A UF é obrigatória.")
        @Pattern(regexp = "^(?i)(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$", message = "A UF deve ser um estado brasileiro válido com 2 letras.")
        String uf,

        @NotBlank(message = "A cidade é obrigatória.")
        String cidade,

        @NotBlank(message = "O bairro é obrigatório.")
        String bairro,

        @NotBlank(message = "O logradouro é obrigatório.")
        String logradouro,

        @NotNull(message = "O número é obrigatório.")
        Integer numero,

        String complemento
) {}