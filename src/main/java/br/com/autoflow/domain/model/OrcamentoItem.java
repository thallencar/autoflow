package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusReservaEstoque;

import java.math.BigDecimal;
import java.util.UUID;

public class OrcamentoItem {

    private UUID id;
    private StatusReservaEstoque statusReserva;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private UUID idEstoque;
    private OrcamentoServico orcamentoServico;
    private Orcamento orcamento;

    public OrcamentoItem() {  }

    public OrcamentoItem(UUID id, StatusReservaEstoque statusReserva, Integer quantidade, BigDecimal valorUnitario, UUID idEstoque) {
        this.id = id != null ? id : UUID.randomUUID();
        this.statusReserva = statusReserva != null ? statusReserva : StatusReservaEstoque.RESERVADO;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.idEstoque = idEstoque;
        calcularTotal();
    }

    public void calcularTotal() {
        if (this.valorUnitario != null && this.quantidade != null) {
            this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }

    public void associarOrcamentoServico(OrcamentoServico orcamentoServico) {
        this.orcamentoServico = orcamentoServico;
        if (orcamentoServico != null) {
            this.orcamento = orcamentoServico.getOrcamento();
        }
    }

    public void alterarStatusReserva(StatusReservaEstoque statusReserva) {
        this.statusReserva = statusReserva;
    }

    public UUID getId() { return id; }
    public StatusReservaEstoque getStatusReserva() { return statusReserva; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public UUID getIdEstoque() { return idEstoque; }
    public OrcamentoServico getOrcamentoServico() { return orcamentoServico; }
    public Orcamento getOrcamento() { return orcamento; }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setOrcamentoServico(OrcamentoServico orcamentoServico) {
        this.orcamentoServico = orcamentoServico;
    }

    public void setStatusReserva(StatusReservaEstoque statusReserva) {
        this.statusReserva = statusReserva;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setIdEstoque(UUID idEstoque) {
        this.idEstoque = idEstoque;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }
}