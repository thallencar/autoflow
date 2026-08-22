package br.com.autoflow.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoResponse(
        UUID idServico,
        String dsServico,
        BigDecimal vlServico,
        Integer qtTempoEstimadoMin
) {}