package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "tp_orcamento", length = 20, nullable = false)
    private String tipoOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_orcamento", nullable = false, length = 15)
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

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItem> itens = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_os", nullable = false)
    private OrdemServico ordemServico;

    public void aprovar() {
        validarMudancaStatus();
        this.status = StatusOrcamento.APROVADO;
        this.dataDecisao = LocalDateTime.now();

        if (this.itens != null) {
            this.itens.forEach(item ->
                    item.setStatusReserva(StatusReservaEstoque.valueOf(String.valueOf(StatusReservaEstoque.VENDIDO))));
        }
    }

    public void recusar() {
        validarMudancaStatus();
        this.status = StatusOrcamento.RECUSADO;
        this.dataDecisao = LocalDateTime.now();

        if (this.itens != null) {
            this.itens.forEach(item ->
                    item.setStatusReserva(StatusReservaEstoque.valueOf(String.valueOf(StatusReservaEstoque.CANCELADO))));
        }
    }

    public void aplicarNovoStatus(StatusOrcamento novoStatus) {
        validarMudancaStatus();
        if (this.status == StatusOrcamento.CANCELADO) {
            throw new RegraNegocioException("Este orçamento expirou e seu status foi atualizado para CANCELADO.");
        }
        if (novoStatus == StatusOrcamento.APROVADO) {
            aprovar();
        } else if (novoStatus == StatusOrcamento.RECUSADO) {
            recusar();
        } else {
            throw new RegraNegocioException("Transição de status não permitida.");
        }
    }

    private void validarMudancaStatus() {
        if (this.dataExpiracao != null && LocalDateTime.now().isAfter(this.dataExpiracao)) {
            this.status = StatusOrcamento.CANCELADO;
            if (this.itens != null) {
                this.itens.forEach(item -> item.setStatusReserva(StatusReservaEstoque.CANCELADO));
            }
            return;
        }
        if (this.status != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos PENDENTES podem ter o status alterado.");
        }
    }
}
