package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusOrcamento;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusOrcamentoRequest(
        @NotNull(message = "O novo status é obrigatório")
        StatusOrcamento status
) {}
