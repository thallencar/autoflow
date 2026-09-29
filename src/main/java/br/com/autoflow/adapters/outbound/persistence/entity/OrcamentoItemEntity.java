package br.com.autoflow.adapters.outbound.persistence.entity;

import br.com.autoflow.domain.enums.StatusReservaEstoque;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable; // 1. Importa o Persistable

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "TB_ORCAMENTO_ITENS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrcamentoItemEntity implements Persistable<UUID> {

    @Id
    // 3. REMOVE o @GeneratedValue para evitar que o Hibernate controle mal o UUID
    @Column(name = "id_orcamento_item", updatable = false, nullable = false)
    private UUID id;

    @Builder.Default
    @Column(name = "st_reserva_estoque", length = 15, nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusReservaEstoque statusReserva = StatusReservaEstoque.RESERVADO;

    @Column(name = "qt_item", nullable = false)
    private Integer quantidade;

    @Column(name = "vl_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "vl_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "id_estoque", nullable = true)
    private UUID idEstoque;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orcamento", nullable = false)
    private OrcamentoEntity orcamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orcamento_servico", nullable = false)
    private OrcamentoServicoEntity orcamentoServico;

    @Transient
    @Builder.Default
    private boolean novo = true;

    @Override
    @Transient
    public boolean isNew() {
        return this.novo || this.id == null;
    }

    @PostPersist
    @PostLoad
    public void marcarComoNaoNovo() {
        this.novo = false;
    }

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }

        if (this.valorUnitario != null && this.quantidade != null) {
            this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        }
    }
}