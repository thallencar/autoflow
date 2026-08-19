package br.com.autoflow.domain.model;

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
public class OrcamentoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_orcamento_servico", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "vl_mao_de_obra", precision = 10, scale = 2, nullable = false)
    private BigDecimal maoDeObra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;

    @Builder.Default
    @OneToMany(mappedBy = "orcamentoServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItem> itens = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orcamento")
    private Orcamento orcamento;

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
        if (this.itens != null) {
            this.itens.forEach(item -> item.setOrcamento(orcamento));
        }
    }

    public void adicionarItem(OrcamentoItem item) {
        this.itens.add(item);
        item.setOrcamentoServico(this);
        if (this.orcamento != null) {
            item.setOrcamento(this.orcamento);
        }
    }
}