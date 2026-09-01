package br.com.autoflow.domain.enums;

import lombok.Getter;

@Getter
public enum StatusReservaEstoque {
    RESERVADO("Reservado"),
    VENDIDO("Vendido"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusReservaEstoque(String descricao) {
        this.descricao = descricao;
    }
}
