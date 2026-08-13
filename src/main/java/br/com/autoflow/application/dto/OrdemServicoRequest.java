package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusOS;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrdemServicoRequest(
        @NotBlank(message = "O relato do cliente é obrigatório")
        String dsRelatoCliente,

        String dsDiagnostico,
        Boolean stTermoAceito,
        LocalDateTime dtAceiteTermo,
        Integer nrKmEntrada,
        StatusOS stOs,
        String stPagamento,
        String dsMotivoCancelamento,
        String placaVeiculo,

        @NotNull(message = "O ID do cliente é obrigatório")
        UUID idCliente,

        @NotNull(message = "O ID do veículo é obrigatório")
        UUID idVeiculo,

        @NotNull(message = "O ID do funcionário é obrigatório")
        UUID idFuncionario,

        @NotNull(message = "O ID do orçamento é obrigatório")
        UUID idOrcamento
) {}