package br.com.autoflow.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TB_ORCAMENTO_SERVICOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrcamentoServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_orcamento_servico", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "vl_mao_de_obra", precision = 10, scale = 2, nullable = false)
    private BigDecimal maoDeObra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private ServicoEntity servico;

    @Builder.Default
    @OneToMany(mappedBy = "orcamentoServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItemEntity> itens = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orcamento")
    private OrcamentoEntity orcamento;

    public void setOrcamento(OrcamentoEntity orcamento) {
        this.orcamento = orcamento;
        if (this.itens != null) {
            this.itens.forEach(item -> item.setOrcamento(orcamento));
        }
    }
}