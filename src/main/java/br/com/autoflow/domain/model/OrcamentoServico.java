package br.com.autoflow.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrcamentoServico {
    private UUID id;
    private BigDecimal maoDeObra;
    private Servico servico;
    private List<OrcamentoItem> itens = new ArrayList<>();
    private Orcamento orcamento;

    public OrcamentoServico() { }

    public OrcamentoServico(UUID id, BigDecimal maoDeObra, Servico servico, List<OrcamentoItem> itens, Orcamento orcamento) {
        this.id = id;
        this.maoDeObra = maoDeObra;
        this.servico = servico;
        this.itens = itens != null ? itens : new ArrayList<>();
        this.orcamento = orcamento;
        atualizarVinculoOrcamentoNosItens();
    }

    private void atualizarVinculoOrcamentoNosItens() {
        if (this.itens != null) {
            this.itens.forEach(item -> {
                // Lógica de vínculo se necessário
            });
        }
    }

    public UUID getId() { return id; }
    public BigDecimal getMaoDeObra() { return maoDeObra; }
    public Servico getServico() { return servico; }
    public List<OrcamentoItem> getItens() { return itens; }
    public Orcamento getOrcamento() { return orcamento; }

    public void setId(UUID id) { this.id = id; }
    public void setMaoDeObra(BigDecimal maoDeObra) { this.maoDeObra = maoDeObra; }
    public void setServico(Servico servico) { this.servico = servico; }
    public void setItens(List<OrcamentoItem> itens) {
        this.itens = itens != null ? itens : new ArrayList<>();
        atualizarVinculoOrcamentoNosItens();
    }
    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
        atualizarVinculoOrcamentoNosItens();
    }
}