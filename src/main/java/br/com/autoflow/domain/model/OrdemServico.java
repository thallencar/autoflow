package br.com.autoflow.domain.entity;

import br.com.autoflow.domain.enums.StatusOS;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_ORDENS_SERVICOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_os", nullable = false)
    private UUID idOs;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_os", nullable = false, length = 30)
    private StatusOS stOs = StatusOS.RECEBIDA;

    @Column(name = "ds_relato_cliente", nullable = false, length = 255)
    private String dsRelatoCliente;

    @Column(name = "ds_diagnostico", length = 255)
    private String dsDiagnostico;

    @Builder.Default
    @Column(name = "st_termo_aceito", nullable = false)
    private Boolean stTermoAceito = false;

    @Column(name = "dt_aceite_termo")
    private LocalDateTime dtAceiteTermo;

    @Column(name = "nr_km_entrada")
    private Integer nrKmEntrada;

    @Column(name = "dt_abertura_os", nullable = false)
    private LocalDateTime dtAberturaOs;

    @Column(name = "dt_incio_diagnostico")
    private LocalDateTime dtIncioDiagnostico;

    @Column(name = "dt_fim_diagnostico")
    private LocalDateTime dtFimDiagnostico;

    @Column(name = "dt_aprovacao_orcamento")
    private LocalDateTime dtAprovacaoOrcamento;

    @Column(name = "dt_encerramento_os")
    private LocalDateTime dtEncerramentoOs;

    @Column(name = "dt_reagendamento_os")
    private LocalDateTime dtReagendamentoOs;

    @Builder.Default
    @Column(name = "st_pagamento", nullable = false, length = 15)
    private String stPagamento = "Pendente";

    @Column(name = "ds_motivo_cancelamento", length = 255)
    private String dsMotivoCancelamento;

    @Column(name = "id_cliente", nullable = false)
    private UUID idCliente;

    @Column(name = "id_veiculo", nullable = false)
    private UUID idVeiculo;

    @Column(name = "id_funcionario", nullable = false)
    private UUID idFuncionario;

    @Column(name = "id_orcamento", nullable = false)
    private UUID idOrcamento;

    @PrePersist
    public void prePersist() {
        if (this.dtAberturaOs == null) {
            this.dtAberturaOs = LocalDateTime.now();
        }
    }
}