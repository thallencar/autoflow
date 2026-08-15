package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private StatusOS statusOS = StatusOS.RECEBIDA;

    @Column(name = "ds_relato_cliente", nullable = false, length = 255)
    private String dsRelatoCliente;

    @Column(name = "ds_diagnostico", length = 255)
    private String dsDiagnostico;

    @Builder.Default
    @Column(name = "st_termo_aceito", nullable = false, updatable = false)
    private Boolean stTermoAceito = false;

    @Column(name = "dt_aceite_termo", updatable = false)
    private LocalDateTime dtAceiteTermo;

    @Column(name = "nr_km_entrada")
    private Integer nrKmEntrada;

    @Column(name = "dt_abertura_os", nullable = false,updatable = false)
    private LocalDateTime dtAberturaOs;

    @Column(name = "dt_inicio_diagnostico")
    private LocalDateTime dtInicioDiagnostico;

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

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_os")
    @Builder.Default
    private List<Orcamento> idsOrcamento = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.dtAberturaOs == null) {
            this.dtAberturaOs = LocalDateTime.now();
        }
    }
    public void atualizarStatus(StatusOS novoStatus, String observacao) {
        if (this.statusOS != null && !this.statusOS.podeTransitarPara(novoStatus)) {
            throw new RegraNegocioException(
                    String.format("Transição de status inválida: não é permitido alterar de %s para %s.",
                            this.statusOS, novoStatus)
            );
        }
        LocalDateTime agora = LocalDateTime.now();
        switch (novoStatus) {
            case EM_DIAGNOSTICO -> {
                if (this.dtInicioDiagnostico == null) {
                    this.dtInicioDiagnostico = agora;
                }
            }
            case AGUARDANDO_APROVACAO -> {
                if (this.dtInicioDiagnostico == null) {
                    this.dtInicioDiagnostico = agora;
                }
                this.dtFimDiagnostico = agora;
            }
            case ORCAMENTO_APROVADO -> {
                if (this.dtInicioDiagnostico == null) this.dtInicioDiagnostico = agora;
                if (this.dtFimDiagnostico == null) this.dtFimDiagnostico = agora;
                if (this.dtAprovacaoOrcamento == null) {
                    this.dtAprovacaoOrcamento = agora;
                }
            }
            case EM_EXECUCAO -> {
                if (this.dtInicioDiagnostico == null) this.dtInicioDiagnostico = agora;
                if (this.dtFimDiagnostico == null) this.dtFimDiagnostico = agora;
                if (this.dtAprovacaoOrcamento == null) this.dtAprovacaoOrcamento = agora;
            }
            case FINALIZADA, ENTREGUE -> {
                if (this.dtEncerramentoOs == null) {
                    this.dtEncerramentoOs = agora;
                }
            }
            case CANCELADA -> {
                this.dtEncerramentoOs = agora;
                this.dsMotivoCancelamento = observacao;
            }
            case RECEBIDA -> { /* Estado inicial */ }
        }
        this.statusOS = novoStatus;
    }
}