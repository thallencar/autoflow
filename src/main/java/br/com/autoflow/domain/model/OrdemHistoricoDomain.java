package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemHistoricoDomain(
        UUID idOs,
        StatusOS statusOS,
        String relatoCliente,
        String diagnostico,
        Integer kmEntrada,
        LocalDateTime dataAbertura,
        LocalDateTime dataEncerramento,
        List<ServicoHistoricoDomain> servicosExecucao
) {
    public record ServicoHistoricoDomain(
            UUID idServico,
            String nomeServico,
            Double valorMaoDeObra,
            List<PecaHistoricoDomain> pecasUtilizadas
    ) {}

    public record PecaHistoricoDomain(
            UUID idEstoque,
            String nomePeca,
            Integer quantidade,
            Double valorUnitario
    ) {}
}