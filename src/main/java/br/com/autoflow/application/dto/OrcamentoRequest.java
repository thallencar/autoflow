package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.TipoOrcamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrcamentoRequest(
        @NotNull(message = "O ID da Ordem de Serviço é obrigatório")
        UUID idOs,

        @NotNull(message = "O tipo do orçamento é obrigatório")
        TipoOrcamento tipoOrcamento,

        @NotNull(message = "A data de expiração é obrigatória")
        LocalDateTime dataExpiracao,

        @NotEmpty(message = "O orçamento deve conter pelo menos um serviço")
        @Valid
        List<OrcamentoServicoRequest> servicos,

        @Valid
        List<OrcamentoItemRequest> itens
) {}