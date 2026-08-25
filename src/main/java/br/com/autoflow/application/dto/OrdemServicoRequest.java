package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusOS;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemServicoRequest(

        @NotBlank(message = "O relato do cliente é obrigatório")
        String dsRelatoCliente,

        String dsDiagnostico,

        @NotNull(message = "Status do Termo de Acite é obrigatório")
        Boolean stTermoAceito,

        LocalDateTime  dtAberturaOs,

        LocalDateTime dtAceiteTermo,

        @NotNull(message = "A Quilometragem do veículo precisa ser preenchida")
        Integer nrKmEntrada,

        StatusOS statusOS,

        String stPagamento,

        String dsMotivoCancelamento,

        @NotNull(message = "O ID do cliente é obrigatório")
        UUID idCliente,

       @NotNull(message = "O ID do veículo é obrigatório")
        UUID idVeiculo,

        UUID idFuncionario,

        List<UUID> idsOrcamento
) {}