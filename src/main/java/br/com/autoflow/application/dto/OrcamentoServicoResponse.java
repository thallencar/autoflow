package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrcamentoServicoResponse(

        UUID id,
        UUID idServico,
        String descricaoServico,
        BigDecimal maoDeObra,
        List<OrcamentoItemResponse> itens
) {
}
