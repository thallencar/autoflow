package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "vl_taxa_permanencia", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxaPermanencia = BigDecimal.ZERO;

    @Column(name = "id_cliente", nullable = false)
    private UUID idCliente;

    @Column(name = "id_veiculo", nullable = false)
    private UUID idVeiculo;

    @Column(name = "id_funcionario", nullable = true)
    private UUID idFuncionario;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
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
        if (this.taxaPermanencia == null) {
            this.taxaPermanencia = BigDecimal.ZERO;
        }
    }

    public void carregarServicosDosOrcamentosAprovados() {
        List<Servico> servicosAprovados = this.idsOrcamento.stream()
                .filter(orcamento -> orcamento.getStatus() == StatusOrcamento.APROVADO)
                .flatMap(orcamento -> orcamento.getServicos().stream())
                .map(OrcamentoServico::getServico)
                .distinct()
                .toList();
        for (Servico servico : servicosAprovados) {
            boolean jaExiste = this.servicosExecucao.stream()
                    .anyMatch(osServico -> osServico.getServico().getIdServico().equals(servico.getIdServico()));

            if (!jaExiste) {
                OsServico osServico = OsServico.builder()
                        .ordemServico(this)
                        .servico(servico)
                        .build();
                this.servicosExecucao.add(osServico);
            }
        }
    }

    public void atualizarStatus(StatusOS novoStatus, String observacao) {
        validarTransicao(novoStatus);
        validarRequisitosOrcamento(novoStatus);
        processarDiagnosticoEObservacao(novoStatus, observacao);

        LocalDateTime agora = LocalDateTime.now();
        executarMudancaStatus(novoStatus, agora, observacao);

        this.statusOS = novoStatus;
    }

    private void validarTransicao(StatusOS novoStatus) {
        if (this.statusOS != null && !this.statusOS.podeTransitarPara(novoStatus)) {
            throw new RegraNegocioException(
                    String.format("Transição de status inválida: não é permitido alterar de %s para %s.",
                            this.statusOS, novoStatus)
            );
        }
    }

    private void processarDiagnosticoEObservacao(StatusOS novoStatus, String observacao) {
        if (novoStatus == StatusOS.EM_DIAGNOSTICO || novoStatus == StatusOS.AGUARDANDO_APROVACAO) {
            if (observacao != null && !observacao.isBlank()) {
                if (this.dsDiagnostico != null && !this.dsDiagnostico.isBlank()) {
                    this.dsDiagnostico = this.dsDiagnostico + " | " + observacao;
                } else {
                    this.dsDiagnostico = observacao;
                }
            }
        }
    }

    private void validarRequisitosOrcamento(StatusOS novoStatus) {
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
    }

    private void executarMudancaStatus(StatusOS novoStatus, LocalDateTime agora, String observacao) {
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
            case RECEBIDA, ABANDONADO -> {
            }
            default -> {
            }
        }
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

    public void verificarCancelamentoAutomatico(int diasLimite, BigDecimal valorDiaria) {
        if (this.statusOS == StatusOS.AGUARDANDO_APROVACAO && this.dtFimDiagnostico != null) {
            long diasDecorridos = java.time.temporal.ChronoUnit.DAYS.between(this.dtFimDiagnostico, LocalDateTime.now());
            if (diasDecorridos > diasLimite) {
                long diasExcedidos = diasDecorridos - diasLimite;
                this.statusOS = StatusOS.CANCELADA;
                this.dtEncerramentoOs = LocalDateTime.now();
                this.dsMotivoCancelamento = "Cancelado automaticamente após " + diasLimite + " dias sem retorno do orçamento (Art. 40 CDC).";
                this.taxaPermanencia = valorDiaria.multiply(BigDecimal.valueOf(diasExcedidos));
                recusarOrcamentosVinculados();
            }
        }
    }

    public void verificarAbandonoTecnico(int diasLimiteAbandono) {
        if (this.statusOS == StatusOS.AGUARDANDO_APROVACAO && this.dtFimDiagnostico != null) {
            long diasDecorridos = java.time.temporal.ChronoUnit.DAYS.between(this.dtFimDiagnostico, LocalDateTime.now());
            if (diasDecorridos >= diasLimiteAbandono) {
                this.statusOS = StatusOS.ABANDONADO;
                this.dtEncerramentoOs = LocalDateTime.now();
                this.dsMotivoCancelamento = "Veículo considerado abandonado após " + diasLimiteAbandono + " dias sem manifestação do cliente.";
            }
        }
    }
}