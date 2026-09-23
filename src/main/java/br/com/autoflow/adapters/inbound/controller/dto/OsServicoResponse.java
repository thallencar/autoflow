package br.com.autoflow.adapters.inbound.controller.dto;

import java.util.UUID;

public record OsServicoResponse(
        UUID id,
        ServicoResponse servico
) {}