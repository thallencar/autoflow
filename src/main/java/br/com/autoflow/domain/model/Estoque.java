package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.TipoItemEstoque;
import java.math.BigDecimal;
import java.util.UUID;

public class Estoque {
    private UUID id;
    private String nomeItem;
    private String nomeMarca;
    private BigDecimal valorUnitario;
    private Integer quantidadeEstoque;
    private Integer quantidadeMinima;
    private TipoItemEstoque tipoCategoria;

    public Estoque(UUID id, String nomeItem, String nomeMarca, BigDecimal valorUnitario, Integer quantidadeEstoque, Integer quantidadeMinima, TipoItemEstoque tipoCategoria) {
        this.id = id;
        this.nomeItem = nomeItem;
        this.nomeMarca = nomeMarca;
        this.valorUnitario = valorUnitario;
        this.quantidadeEstoque = quantidadeEstoque != null ? quantidadeEstoque : 0;
        this.quantidadeMinima = quantidadeMinima != null ? quantidadeMinima : 0;
        this.tipoCategoria = tipoCategoria;
    }

    public void adicionarQuantidade(Integer qtd) {
        if (qtd != null && qtd > 0) {
            this.quantidadeEstoque += qtd;
        }
    }

    public void removerQuantidade(Integer qtd) {
        if (qtd != null && qtd > 0) {
            this.quantidadeEstoque -= qtd;
        }
    }

    public void atualizarValorUnitario(BigDecimal novoValor) {
        if (novoValor != null) {
            this.valorUnitario = novoValor;
        }
    }

    public void atualizarDados(String nomeItem, String nomeMarca, BigDecimal valorUnitario, Integer quantidadeEstoque, Integer quantidadeMinima, TipoItemEstoque tipoCategoria) {
        this.nomeItem = nomeItem;
        this.nomeMarca = nomeMarca;
        this.valorUnitario = valorUnitario;
        this.quantidadeEstoque = quantidadeEstoque != null ? quantidadeEstoque : 0;
        this.quantidadeMinima = quantidadeMinima != null ? quantidadeMinima : 0;
        this.tipoCategoria = tipoCategoria;
    }

    public boolean deveDispararAlertaEstoqueBaixo() {
        boolean estoqueBaixo = (this.quantidadeEstoque != null && this.quantidadeMinima != null)
                && (this.quantidadeEstoque <= this.quantidadeMinima);
        boolean ehAlertaGeral = this.tipoCategoria == TipoItemEstoque.INSUMO;
        return estoqueBaixo && ehAlertaGeral;
    }

    public UUID getId() { return id; }
    public String getNomeItem() { return nomeItem; }
    public String getNomeMarca() { return nomeMarca; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public Integer getQuantidadeEstoque() { return quantidadeEstoque; }
    public Integer getQuantidadeMinima() { return quantidadeMinima; }
    public TipoItemEstoque getTipoCategoria() { return tipoCategoria; }

    public void setId(UUID id) { this.id = id; }
    public void setNomeItem(String nomeItem) { this.nomeItem = nomeItem; }
    public void setNomeMarca(String nomeMarca) { this.nomeMarca = nomeMarca; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque != null ? quantidadeEstoque : 0;
    }
    public void setQuantidadeMinima(Integer quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima != null ? quantidadeMinima : 0;
    }
    public void setTipoCategoria(TipoItemEstoque tipoCategoria) { this.tipoCategoria = tipoCategoria; }
}