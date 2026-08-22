package br.com.autoflow.application.dto;

import java.util.UUID;

public record VeiculoResponse(
        UUID id,
        String placa,
        String marca,
        String modelo,
        Integer kmAtual,
        Short anoFabricacao,
        String cor,
        UUID clienteId
) {}
