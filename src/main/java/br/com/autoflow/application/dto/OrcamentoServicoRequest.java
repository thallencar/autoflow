package br.com.autoflow.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrcamentoServicoRequest(

        @NotNull(message = "O ID do serviço é obrigatório.")
        UUID idServico,

        @NotNull(message = "O valor unitário é obrigatório.")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal maoDeObra,

        @NotNull(message = "Obrigatório preenchimento das peças/insumos.")
        List<OrcamentoItemRequest> itens
) {
}
