package br.com.autoflow.adapters.inbound.controller.dto;

import br.com.autoflow.domain.enums.Genero;

import java.time.LocalDate;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String documento,
        String email,
        LocalDate dataNascimento,
        String telefone,
        Genero genero,
        EnderecoResponse endereco
) {}
