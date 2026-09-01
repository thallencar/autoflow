package br.com.autoflow.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class OsServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_os_servico", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_os", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;

    @Column(name = "dt_inicio_servico")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "dt_fim_servico")
    private LocalDateTime dataFimExecucao;
}