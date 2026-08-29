package br.com.autoflow.application.dto;

import java.util.UUID;

public record OsServicoResponse(
        UUID id,
        ServicoResponse servico
) {}