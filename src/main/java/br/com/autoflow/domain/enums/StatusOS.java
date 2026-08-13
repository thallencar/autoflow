package br.com.autoflow.domain.enums;

import lombok.Getter;

@Getter
public enum StatusOS {
    RECEBIDA("Recebida"),
    EM_DIAGNOSTICO("Em diagnóstico"),
    AGUARDANDO_APROVACAO("Aguardando aprovação"),
    EM_EXECUCAO("Em execução"),
    FINALIZADA("Finalizada"),
    ENTREGUE("Entregue");

    private final String descricao;

    StatusOS(String descricao) {
        this.descricao = descricao;
    }
}