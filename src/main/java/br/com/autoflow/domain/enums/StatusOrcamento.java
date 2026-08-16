package br.com.autoflow.domain.enums;

import lombok.Getter;

@Getter
public enum StatusOrcamento {
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado"),
    EXPIRADO("Expirado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusOrcamento(String descricao) {
        this.descricao = descricao;
    }
}

