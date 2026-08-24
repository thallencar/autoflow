package br.com.autoflow.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MetricaOsResponse(
        UUID idOs,
        String statusOS,
        Integer tempoTotalEstimadoMinutos,
        Long tempoTotalExecucaoMinutos,
        Long diferencaMinutos, // Negativo = entregou antes; Positivo = atrasou
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataFimExecucao
) {}