package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.TipoItemEstoque;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "TB_ESTOQUES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_estoque", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nm_item", length = 100, nullable = false)
    private String nomeItem;

    @Column(name = "nm_marca", length = 20, nullable = false)
    private String nomeMarca;

    @Column(name = "vl_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Builder.Default
    @Column(name = "qtd_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Builder.Default
    @Column(name = "qtd_minima")
    private Integer quantidadeMinima = 0;

    @Column(name = "tp_categoria", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoItemEstoque tipoCategoria;

    public boolean deveDispararAlertaEstoqueBaixo() {
        boolean estoqueBaixo = this.quantidadeEstoque != null && this.quantidadeMinima != null
                && this.quantidadeEstoque <= this.quantidadeMinima;

        boolean ehInsumoOuPeca = this.tipoCategoria == TipoItemEstoque.INSUMO
                || this.tipoCategoria == TipoItemEstoque.PECA;

        return estoqueBaixo && ehInsumoOuPeca;
    }
}