package br.com.autoflow.application.dto;

import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VeiculoRequest(

        @NotBlank(message = "A placa é obrigatória.")
        @Pattern(
                regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$",
                message = "A placa deve estar no formato antigo (AAA1234) ou Mercosul (AAA1A23)."
        )
        String placa,

        @NotBlank(message = "A marca é obrigatória.")
        @Size(max = 50, message = "A marca deve ter no máximo 50 caracteres.")
        String marca,

        @NotBlank(message = "O modelo é obrigatório.")
        @Size(max = 50, message = "O modelo deve ter no máximo 50 caracteres.")
        String modelo,

        @NotNull(message = "O ano de fabricação é obrigatório.")
        @Min(value = 1900, message = "O ano de fabricação deve ser maior que 1900.")
        Short anoFabricacao,

        @NotBlank(message = "A cor é obrigatória.")
        @Size(max = 30, message = "A cor deve ter no máximo 30 caracteres.")
        String cor,

        @NotNull(message = "O ID do cliente é obrigatório.")
        UUID clienteId
) {}
