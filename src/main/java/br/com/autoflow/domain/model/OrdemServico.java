package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
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
    private StatusPagamento stPagamento = StatusPagamento.PENDENTE;

    @Column(name = "ds_motivo_cancelamento", length = 255)
    private String dsMotivoCancelamento;

    @Column(name = "id_cliente", nullable = false)
    private UUID idCliente;

    @Column(name = "id_veiculo", nullable = false)
    private UUID idVeiculo;

    @Column(name = "id_funcionario", nullable = true)
    private UUID idFuncionario;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_os")
    @Builder.Default
    private List<Orcamento> idsOrcamento = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OsServico> servicosExecucao = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.dtAberturaOs == null) {
            this.dtAberturaOs = LocalDateTime.now();
        }
        if (this.stPagamento == null) {
            this.stPagamento = StatusPagamento.PENDENTE;
        }
    }

    public void carregarServicosDosOrcamentosAprovados() {
        this.servicosExecucao.clear();

        this.idsOrcamento.stream()
                .filter(orcamento -> orcamento.getStatus() == StatusOrcamento.APROVADO)
                .flatMap(orcamento -> orcamento.getServicos().stream())
                .forEach(orcamentoServico -> {
                    OsServico osServico = OsServico.builder()
                            .ordemServico(this)
                            .servico(orcamentoServico.getServico())
                            .build();

                    this.servicosExecucao.add(osServico);
                });
    }

    public void atualizarStatus(StatusOS novoStatus, String observacao) {
        if (this.statusOS != null && !this.statusOS.podeTransitarPara(novoStatus)) {
            throw new RegraNegocioException(
                    String.format("Transição de status inválida: não é permitido alterar de %s para %s.",
                            this.statusOS, novoStatus)
            );
        }
        if (novoStatus == StatusOS.EM_DIAGNOSTICO || novoStatus == StatusOS.AGUARDANDO_APROVACAO) {
            if (observacao != null && !observacao.isBlank()) {
                this.dsDiagnostico = observacao;
            }
        }
        if (novoStatus == StatusOS.AGUARDANDO_APROVACAO) {
            if (this.dsDiagnostico == null || this.dsDiagnostico.isBlank()) {
                throw new RegraNegocioException("A descrição do diagnóstico do mecânico é obrigatória para finalizar a etapa de diagnóstico.");
            }
        }
        List<StatusOS> statusPosDiagnostico = List.of(
                StatusOS.AGUARDANDO_APROVACAO,
                StatusOS.ORCAMENTO_APROVADO,
                StatusOS.EM_EXECUCAO
        );

        if (statusPosDiagnostico.contains(novoStatus)) {
            if (this.idsOrcamento == null || this.idsOrcamento.isEmpty()) {
                throw new RegraNegocioException("Não é possível avançar de etapa sem ao menos um orçamento vinculado à Ordem de Serviço.");
            }
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
                aprovarOrcamentosVinculados(agora);
                carregarServicosDosOrcamentosAprovados();
            }
            case EM_EXECUCAO -> {
                if (this.dtInicioDiagnostico == null) this.dtInicioDiagnostico = agora;
                if (this.dtFimDiagnostico == null) this.dtFimDiagnostico = agora;
                if (this.dtAprovacaoOrcamento == null) this.dtAprovacaoOrcamento = agora;
                if (this.dataInicioExecucao == null) {
                    this.dataInicioExecucao = agora;
                }
                aprovarOrcamentosVinculados(agora);
                if (this.servicosExecucao.isEmpty()) {
                    carregarServicosDosOrcamentosAprovados();
                }
            }
            case FINALIZADA -> {
                if (this.dataFimExecucao == null) {
                    this.dataFimExecucao = agora;
                }
            }
            case ENTREGUE -> {
                if (this.stPagamento == StatusPagamento.PENDENTE) {
                    throw new RegraNegocioException("Não é possível entregar o veículo enquanto o pagamento estiver pendente.");
                }
                if (this.dataFimExecucao == null) {
                    this.dataFimExecucao = agora;
                }
                if (this.dtEncerramentoOs == null) {
                    this.dtEncerramentoOs = agora;
                }
            }
            case CANCELADA -> {
                this.dtEncerramentoOs = agora;
                this.dsMotivoCancelamento = observacao;
                recusarOrcamentosVinculados();
            }
            case RECEBIDA -> { /* Estado inicial */ }
        }
        this.statusOS = novoStatus;
    }

    public Long getTempoTotalExecucaoMinutos() {
        if (this.dataInicioExecucao != null && this.dataFimExecucao != null) {
            return java.time.Duration.between(this.dataInicioExecucao, this.dataFimExecucao).toMinutes();
        }
        return null;
    }

    public Integer getTempoTotalEstimadoMinutos() {
        if (this.servicosExecucao == null || this.servicosExecucao.isEmpty()) {
            return 0;
        }
        return this.servicosExecucao.stream()
                .map(OsServico::getServico)
                .filter(java.util.Objects::nonNull)
                .map(Servico::getQtTempoEstimadoMin)
                .filter(java.util.Objects::nonNull)
                .reduce(0, Integer::sum);
    }

    public Long getDiferencaMinutos() {
        Long tempoGasto = getTempoTotalExecucaoMinutos();
        Integer tempoEstimado = getTempoTotalEstimadoMinutos();
        if (tempoGasto != null && tempoEstimado != null) {
            return tempoGasto - tempoEstimado;
        }
        return null;
    }

    private void aprovarOrcamentosVinculados(LocalDateTime dataAprovacao) {
        if (this.idsOrcamento != null) {
            this.idsOrcamento.forEach(orcamento -> {
                if (orcamento.getStatus() == StatusOrcamento.PENDENTE) {
                    orcamento.aprovar();
                }
            });
        }
    }

    private void recusarOrcamentosVinculados() {
        if (this.idsOrcamento != null) {
            this.idsOrcamento.forEach(orcamento -> {
                if (orcamento.getStatus() == StatusOrcamento.PENDENTE || orcamento.getStatus() == StatusOrcamento.APROVADO) {
                    orcamento.recusar();
                }
            });
        }
    }
}