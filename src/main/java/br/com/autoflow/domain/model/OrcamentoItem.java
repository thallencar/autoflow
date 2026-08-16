package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusReservaEstoque;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "TB_ORCAMENTO_ITENS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrcamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(name = "id_estoque", nullable = false)
    private UUID idEstoque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orcamento", nullable = false)
    private Orcamento orcamento;
}