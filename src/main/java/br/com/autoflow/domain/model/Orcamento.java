package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.exception.RegraNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Orcamento {
    private UUID id;
    private TipoOrcamento tipoOrcamento;
    private StatusOrcamento status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataDecisao;
    private BigDecimal subtotalPecas;
    private BigDecimal maoObra;
    private BigDecimal total;
    private OrdemServico ordemServico;
    private List<OrcamentoServico> servicos;
    private List<OrcamentoItem> itens;

    public Orcamento() {}

    public Orcamento(UUID id, TipoOrcamento tipoOrcamento, StatusOrcamento status, LocalDateTime dataCriacao,
                     LocalDateTime dataExpiracao, LocalDateTime dataDecisao, BigDecimal subtotalPecas,
                     BigDecimal maoObra, BigDecimal total, OrdemServico ordemServico,
                     List<OrcamentoServico> servicos, List<OrcamentoItem> itens) {
        this.id = id;
        this.tipoOrcamento = tipoOrcamento;
        this.status = status != null ? status : StatusOrcamento.PENDENTE;
        this.dataCriacao = dataCriacao;
        this.dataExpiracao = dataExpiracao;
        this.dataDecisao = dataDecisao;
        this.subtotalPecas = subtotalPecas != null ? subtotalPecas : BigDecimal.ZERO;
        this.maoObra = maoObra != null ? maoObra : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.ordemServico = ordemServico;
        this.servicos = servicos != null ? servicos : new ArrayList<>();
        this.itens = itens != null ? itens : new ArrayList<>();
        recalcularTotais();
    }

    public void aprovar() {
        validarMudancaStatus();
        this.status = StatusOrcamento.APROVADO;
        this.dataDecisao = LocalDateTime.now(ZoneId.systemDefault());
        atualizarStatusReservaItens(StatusReservaEstoque.VENDIDO);
    }

    public void recusar() {
        validarMudancaStatus();
        this.status = StatusOrcamento.RECUSADO;
        this.dataDecisao = LocalDateTime.now(ZoneId.systemDefault());
        atualizarStatusReservaItens(StatusReservaEstoque.CANCELADO);
    }

    public void expirar() {
        this.status = StatusOrcamento.CANCELADO;
        this.dataDecisao = LocalDateTime.now(ZoneId.systemDefault());
        atualizarStatusReservaItens(StatusReservaEstoque.CANCELADO);
    }

    public void aplicarNovoStatus(StatusOrcamento novoStatus) {
        switch (novoStatus) {
            case APROVADO -> aprovar();
            case RECUSADO -> recusar();
            default -> throw new RegraNegocioException(
                    String.format("Transição para o status %s não é permitida.", novoStatus)
            );
        }
    }

    private void atualizarStatusReservaItens(StatusReservaEstoque statusReserva) {
        if (this.servicos != null) {
            this.servicos.stream()
                    .filter(s -> s.getItens() != null)
                    .flatMap(s -> s.getItens().stream())
                    .forEach(item -> item.setStatusReserva(statusReserva));
        }
    }

    private void validarMudancaStatus() {
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos PENDENTES podem ter o status alterado.");
        }
        if (this.dataExpiracao != null && LocalDateTime.now(ZoneId.systemDefault()).isAfter(this.dataExpiracao)) {
            throw new RegraNegocioException("Este orçamento está expirado e não pode mais ser alterado.");
        }
    }

    public void recalcularTotais() {
        this.maoObra = (this.servicos == null) ? BigDecimal.ZERO : this.servicos.stream()
                .map(OrcamentoServico::getMaoDeObra)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotalPecas = (this.servicos == null) ? BigDecimal.ZERO : this.servicos.stream()
                .filter(s -> s.getItens() != null)
                .flatMap(s -> s.getItens().stream())
                .peek(OrcamentoItem::calcularTotal)
                .map(OrcamentoItem::getValorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = this.maoObra.add(this.subtotalPecas);
    }

    // Getters e Setters de Domínio
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TipoOrcamento getTipoOrcamento() { return tipoOrcamento; }
    public void setTipoOrcamento(TipoOrcamento tipoOrcamento) { this.tipoOrcamento = tipoOrcamento; }
    public StatusOrcamento getStatus() { return status; }
    public void setStatus(StatusOrcamento status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(LocalDateTime dataExpiracao) { this.dataExpiracao = dataExpiracao; }
    public LocalDateTime getDataDecisao() { return dataDecisao; }
    public void setDataDecisao(LocalDateTime dataDecisao) { this.dataDecisao = dataDecisao; }
    public BigDecimal getSubtotalPecas() { return subtotalPecas; }
    public BigDecimal getMaoObra() { return maoObra; }
    public BigDecimal getTotal() { return total; }
    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }
    public List<OrcamentoServico> getServicos() { return servicos; }
    public void setServicos(List<OrcamentoServico> servicos) { this.servicos = servicos; recalcularTotais(); }
    public List<OrcamentoItem> getItens() { return itens; }
    public void setItens(List<OrcamentoItem> itens) { this.itens = itens; }
}