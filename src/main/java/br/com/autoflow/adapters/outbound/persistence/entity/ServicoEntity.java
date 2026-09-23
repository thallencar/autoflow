package br.com.autoflow.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "TB_SERVICOS")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_servico", nullable = false, updatable = false)
    private UUID idServico;

    @Column(name = "ds_servico", nullable = false, length = 255)
    private String dsServico;

    @Column(name = "vl_servico", nullable = false, precision = 10, scale = 2)
    private BigDecimal vlServico;

    @Column(name = "qt_tempo_estimado_min", nullable = false)
    private Integer qtTempoEstimadoMin;
}