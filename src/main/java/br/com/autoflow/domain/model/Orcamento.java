package com.projeto.model;

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

    @Builder.Default
    @Column(name = "st_orcamento", length = 15, nullable = false)
    private String status = "Pendente";

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
}