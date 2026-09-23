package br.com.autoflow.adapters.outbound.persistence.entity;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "TB_ORDENS_SERVICOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_os", nullable = false)
    private UUID idOs;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_os", nullable = false, length = 30)
    private StatusOS statusOS;

    @Column(name = "ds_relato_cliente", nullable = false, length = 255)
    private String dsRelatoCliente;

    @Column(name = "ds_diagnostico", length = 255)
    private String dsDiagnostico;

    @Column(name = "st_termo_aceito", nullable = false, updatable = false)
    private Boolean stTermoAceito;

    @Column(name = "dt_aceite_termo", updatable = false)
    private LocalDateTime dtAceiteTermo;

    @Column(name = "nr_km_entrada")
    private Integer nrKmEntrada;

    @Column(name = "dt_abertura_os", nullable = false, updatable = false)
    private LocalDateTime dtAberturaOs;

    @Column(name = "dt_inicio_diagnostico")
    private LocalDateTime dtInicioDiagnostico;

    @Column(name = "dt_fim_diagnostico")
    private LocalDateTime dtFimDiagnostico;

    @Column(name = "dt_aprovacao_orcamento")
    private LocalDateTime dtAprovacaoOrcamento;

    @Column(name = "dt_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "dt_fim_execucao")
    private LocalDateTime dataFimExecucao;

    @Column(name = "dt_encerramento_os")
    private LocalDateTime dtEncerramentoOs;

    @Column(name = "dt_reagendamento_os")
    private LocalDateTime dtReagendamentoOs;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_pagamento", nullable = false, length = 15)
    private StatusPagamento stPagamento;

    @Column(name = "ds_motivo_cancelamento", length = 255)
    private String dsMotivoCancelamento;

    @Column(name = "vl_taxa_permanencia", precision = 10, scale = 2)
    private BigDecimal taxaPermanencia;

    @Column(name = "id_cliente", nullable = false)
    private UUID idCliente;

    @Column(name = "id_veiculo", nullable = false)
    private UUID idVeiculo;

    @Column(name = "id_funcionario", nullable = true)
    private UUID idFuncionario;

    @PrePersist
    public void prePersist() {
        if (this.dtAberturaOs == null) {
            this.dtAberturaOs = LocalDateTime.now(ZoneId.systemDefault());
        }
        if (this.stPagamento == null) {
            this.stPagamento = StatusPagamento.PENDENTE;
        }
        if (this.taxaPermanencia == null) {
            this.taxaPermanencia = BigDecimal.ZERO;
        }
    }
}