package br.com.autoflow.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MetricaOsResponse(
        UUID idOs,
        String statusOS,
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataFimExecucao,
        Long tempoTotalExecucaoMinutos
) {}