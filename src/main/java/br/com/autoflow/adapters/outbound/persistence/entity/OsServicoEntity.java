package br.com.autoflow.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "TB_OS_SERVICOS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_os_servico",
                        columnNames = {"id_os", "id_servico"}
                )
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OsServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_os_servico", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_os", nullable = false)
    private OrdemServicoEntity ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private ServicoEntity servico;

    @Column(name = "dt_inicio_servico")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "dt_fim_servico")
    private LocalDateTime dataFimExecucao;
}