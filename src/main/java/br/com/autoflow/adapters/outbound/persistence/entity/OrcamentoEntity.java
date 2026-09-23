package br.com.autoflow.adapters.outbound.persistence.entity;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
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
public class OrcamentoEntity {

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
    private OrdemServicoEntity ordemServico;

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoServicoEntity> servicos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItemEntity> itens = new ArrayList<>();
}