package br.com.autoflow.adapters.inbound.controller.dto;

import java.util.UUID;

public record EnderecoResponse(
        UUID idEndereco,
        String logradouro,
        int numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep
) {}