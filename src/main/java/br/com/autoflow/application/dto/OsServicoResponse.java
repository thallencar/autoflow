package br.com.autoflow.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OsServicoResponse(
        UUID id,
        ServicoResponse servico
) {}