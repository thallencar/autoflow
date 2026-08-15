package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusOS;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemServicoResponse(
        UUID idOs,
        StatusOS statusOS,
        String dsRelatoCliente,
        String dsDiagnostico,
        Boolean stTermoAceito,
        LocalDateTime dtAceiteTermo,
        Integer nrKmEntrada,
        LocalDateTime dtAberturaOs,
        LocalDateTime dtInicioDiagnostico,
        LocalDateTime dtFimDiagnostico,
        LocalDateTime dtAprovacaoOrcamento,
        LocalDateTime dtEncerramentoOs,
        LocalDateTime dtReagendamentoOs,
        String stPagamento,
        String dsMotivoCancelamento,
        UUID idCliente,
        UUID idVeiculo,
        UUID idFuncionario,
        List<UUID> idsOrcamento
) {}