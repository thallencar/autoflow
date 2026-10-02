package br.com.autoflow.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrcamentoServico {

    private UUID id;
    private BigDecimal maoDeObra;
    private Servico servico;
    private List<OrcamentoItem> itens;
    private Orcamento orcamento;

    public OrcamentoServico() {
    }

    public OrcamentoServico(UUID id, BigDecimal maoDeObra, Servico servico, List<OrcamentoItem> itens) {
        this.id = id != null ? id : UUID.randomUUID();
        this.maoDeObra = maoDeObra != null ? maoDeObra : BigDecimal.ZERO;
        this.servico = servico;
        this.itens = itens != null ? itens : new ArrayList<>();
        vincularItens();
    }

    public void associarOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
        vincularItens();
    }

    private void vincularItens() {
        if (this.itens != null) {
            for (OrcamentoItem item : this.itens) {
                item.associarOrcamentoServico(this);
            }
        }
    }

    public UUID getId() { return id; }
    public BigDecimal getMaoDeObra() { return maoDeObra; }
    public Servico getServico() { return servico; }
    public List<OrcamentoItem> getItens() { return itens; }
    public Orcamento getOrcamento() { return orcamento; }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setMaoDeObra(BigDecimal maoDeObra) {
        this.maoDeObra = maoDeObra;
    }

    public void setItens(List<OrcamentoItem> itens) {
        this.itens = itens;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

}