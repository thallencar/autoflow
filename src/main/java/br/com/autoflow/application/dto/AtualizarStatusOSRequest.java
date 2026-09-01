package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusOS;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarStatusOSRequest(

        @NotNull(message = "O novo status é obrigatório.")
        StatusOS status,

        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
        String observacao // Ex: motivo do cancelamento ou nota do técnico
) {}