package br.com.autoflow.domain.enums;

import lombok.Getter;

@Getter
public enum StatusOS {
    RECEBIDA("Recebida"),
    EM_DIAGNOSTICO("Em diagnóstico"),
    AGUARDANDO_APROVACAO("Aguardando aprovação"),
    EM_EXECUCAO("Em execução"),
    ORCAMENTO_APROVADO("Orçamento Aprovado"),
    FINALIZADA("Finalizada"),
    ENTREGUE("Entregue"),
    ABANDONADO("Abandonado"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusOS(String descricao) {
        this.descricao = descricao;
    }

    public boolean podeTransitarPara(StatusOS novoStatus) {
        if ((this == StatusOS.ORCAMENTO_APROVADO || this == StatusOS.EM_EXECUCAO) && novoStatus == StatusOS.AGUARDANDO_APROVACAO) {
            return true;
        }
        if (this == CANCELADA || this == ENTREGUE) {
            return false;
        }
        if (novoStatus == CANCELADA) {
            return true;
        }
        return switch (this) {
            case RECEBIDA -> novoStatus == EM_DIAGNOSTICO;
            case EM_DIAGNOSTICO -> novoStatus == AGUARDANDO_APROVACAO;
            case AGUARDANDO_APROVACAO -> novoStatus == ORCAMENTO_APROVADO || novoStatus == EM_EXECUCAO;
            case ORCAMENTO_APROVADO -> novoStatus == EM_EXECUCAO;
            case EM_EXECUCAO -> novoStatus == FINALIZADA || novoStatus == AGUARDANDO_APROVACAO;
            case FINALIZADA -> novoStatus == ENTREGUE;
            default -> false;
        };
    }
}