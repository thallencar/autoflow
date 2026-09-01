package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TB_ORCAMENTOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_orcamento", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_orcamento", length = 20, nullable = false)
    private TipoOrcamento tipoOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_orcamento", nullable = false, length = 15)
    @Builder.Default
    private StatusOrcamento status = StatusOrcamento.PENDENTE;

    @Column(name = "dt_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "dt_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "dt_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "vl_subtotal_pecas", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotalPecas;

    @Column(name = "vl_mao_obra", precision = 10, scale = 2, nullable = false)
    private BigDecimal maoObra;

    @Column(name = "vl_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_os", nullable = false)
    private OrdemServico ordemServico;

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoServico> servicos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItem> itens = new ArrayList<>();

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

    @PrePersist
    @PreUpdate
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
}