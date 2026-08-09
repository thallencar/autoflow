package br.com.autoflow.domain.enums;

public enum Perfil {
    ADMIN("admin"),
    MECANICO("mecanico"),
    CLIENTE("cliente");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
