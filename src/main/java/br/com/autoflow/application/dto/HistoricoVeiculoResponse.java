package br.com.autoflow.application.dto;

import br.com.autoflow.domain.enums.StatusOS;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HistoricoVeiculoResponse(
        UUID idOs,
        StatusOS statusOS,
        String relatoCliente,
        String diagnostico,
        Integer kmEntrada,
        LocalDateTime dataAbertura,
        LocalDateTime dataEncerramento,
        List<ServicoHistorico> servicosExecucao
) {
    public record ServicoHistorico(
            UUID idServico,
            String nomeServico, // dsServico
            Double valorMaoDeObra,
            List<PecaHistorico> pecasUtilizadas
    ) {}

    public record PecaHistorico(
            UUID idEstoque,
            String nomePeca,
            Integer quantidade,
            Double valorUnitario
    ) {}
}