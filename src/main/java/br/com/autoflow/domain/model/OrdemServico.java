package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.exception.RegraNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrdemServico {

    private UUID idOs;
    private StatusOS statusOS;
    private String dsRelatoCliente;
    private String dsDiagnostico;
    private Boolean stTermoAceito;
    private LocalDateTime dtAceiteTermo;
    private Integer nrKmEntrada;
    private LocalDateTime dtAberturaOs;
    private LocalDateTime dtInicioDiagnostico;
    private LocalDateTime dtFimDiagnostico;
    private LocalDateTime dtAprovacaoOrcamento;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;
    private LocalDateTime dtEncerramentoOs;
    private LocalDateTime dtReagendamentoOs;
    private StatusPagamento stPagamento;
    private String dsMotivoCancelamento;
    private BigDecimal taxaPermanencia;
    private UUID idCliente;
    private UUID idVeiculo;
    private UUID idFuncionario;
    private List<Orcamento> idsOrcamento;
    private List<OsServico> servicosExecucao;

    public OrdemServico() {
        this.statusOS = StatusOS.RECEBIDA;
        this.stTermoAceito = false;
        this.stPagamento = StatusPagamento.PENDENTE;
        this.taxaPermanencia = BigDecimal.ZERO;
        this.idsOrcamento = new ArrayList<>();
        this.servicosExecucao = new ArrayList<>();
    }

    public OrdemServico(UUID idOs, StatusOS statusOS, String dsRelatoCliente, String dsDiagnostico,
                        Boolean stTermoAceito, LocalDateTime dtAceiteTermo, Integer nrKmEntrada,
                        LocalDateTime dtAberturaOs, LocalDateTime dtInicioDiagnostico, LocalDateTime dtFimDiagnostico,
                        LocalDateTime dtAprovacaoOrcamento, LocalDateTime dataInicioExecucao, LocalDateTime dataFimExecucao,
                        LocalDateTime dtEncerramentoOs, LocalDateTime dtReagendamentoOs, StatusPagamento stPagamento,
                        String dsMotivoCancelamento, BigDecimal taxaPermanencia, UUID idCliente, UUID idVeiculo,
                        UUID idFuncionario, List<Orcamento> idsOrcamento, List<OsServico> servicosExecucao) {
        this.idOs = idOs;
        this.statusOS = statusOS != null ? statusOS : StatusOS.RECEBIDA;
        this.dsRelatoCliente = dsRelatoCliente;
        this.dsDiagnostico = dsDiagnostico;
        this.stTermoAceito = stTermoAceito != null ? stTermoAceito : false;
        this.dtAceiteTermo = dtAceiteTermo;
        this.nrKmEntrada = nrKmEntrada;
        this.dtAberturaOs = dtAberturaOs;
        this.dtInicioDiagnostico = dtInicioDiagnostico;
        this.dtFimDiagnostico = dtFimDiagnostico;
        this.dtAprovacaoOrcamento = dtAprovacaoOrcamento;
        this.dataInicioExecucao = dataInicioExecucao;
        this.dataFimExecucao = dataFimExecucao;
        this.dtEncerramentoOs = dtEncerramentoOs;
        this.dtReagendamentoOs = dtReagendamentoOs;
        this.stPagamento = stPagamento != null ? stPagamento : StatusPagamento.PENDENTE;
        this.dsMotivoCancelamento = dsMotivoCancelamento;
        this.taxaPermanencia = taxaPermanencia != null ? taxaPermanencia : BigDecimal.ZERO;
        this.idCliente = idCliente;
        this.idVeiculo = idVeiculo;
        this.idFuncionario = idFuncionario;
        this.idsOrcamento = idsOrcamento != null ? idsOrcamento : new ArrayList<>();
        this.servicosExecucao = servicosExecucao != null ? servicosExecucao : new ArrayList<>();
    }

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
                OsServico osServico = new OsServico();
                osServico.setOrdemServico(this);
                osServico.setServico(servico);
                this.servicosExecucao.add(osServico);
            }
        }
    }

    public void atualizarStatus(StatusOS novoStatus, String observacao) {
        validarTransicao(novoStatus);
        validarRequisitosOrcamento(novoStatus);
        processarDiagnosticoEObservacao(novoStatus, observacao);

        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
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
        if ((novoStatus == StatusOS.EM_DIAGNOSTICO || novoStatus == StatusOS.AGUARDANDO_APROVACAO) && observacao != null && !observacao.isBlank()) {
            if (this.dsDiagnostico != null && !this.dsDiagnostico.isBlank()) {
                this.dsDiagnostico = this.dsDiagnostico + " | " + observacao;
            } else {
                this.dsDiagnostico = observacao;
            }
        }
    }

    private void validarRequisitosOrcamento(StatusOS novoStatus) {
        List<StatusOS> statusPosDiagnostico = List.of(
                StatusOS.AGUARDANDO_APROVACAO,
                StatusOS.ORCAMENTO_APROVADO,
                StatusOS.EM_EXECUCAO
        );

        if (statusPosDiagnostico.contains(novoStatus) && (this.idsOrcamento == null || this.idsOrcamento.isEmpty())) {
            throw new RegraNegocioException("Não é possível avançar de etapa sem ao menos um orçamento vinculado à Ordem de Serviço.");
        }
    }

    private void executarMudancaStatus(StatusOS novoStatus, LocalDateTime agora, String observacao) {
        switch (novoStatus) {
            case EM_DIAGNOSTICO -> tratarEmDiagnostico(agora);
            case AGUARDANDO_APROVACAO -> tratarAguardandoAprovacao(agora);
            case ORCAMENTO_APROVADO -> tratarOrcamentoAprovado(agora);
            case EM_EXECUCAO -> tratarEmExecucao(agora);
            case FINALIZADA -> tratarFinalizada(agora);
            case ENTREGUE -> tratarEntregue(agora);
            case CANCELADA -> tratarCancelada(agora, observacao);
            case RECEBIDA, ABANDONADO -> { }
            default -> throw new RegraNegocioException("Status de Ordem de Serviço não suportado: " + novoStatus);
        }
    }

    private void tratarEmDiagnostico(LocalDateTime agora) {
        if (this.dtInicioDiagnostico == null) {
            this.dtInicioDiagnostico = agora;
        }
    }

    private void tratarAguardandoAprovacao(LocalDateTime agora) {
        tratarEmDiagnostico(agora);
        this.dtFimDiagnostico = agora;
    }

    private void tratarOrcamentoAprovado(LocalDateTime agora) {
        garantirDatasDiagnostico(agora);
        if (this.dtAprovacaoOrcamento == null) {
            this.dtAprovacaoOrcamento = agora;
        }
        aprovarOrcamentosVinculados();
        carregarServicosDosOrcamentosAprovados();
    }

    private void tratarEmExecucao(LocalDateTime agora) {
        garantirDatasDiagnostico(agora);
        if (this.dtAprovacaoOrcamento == null) {
            this.dtAprovacaoOrcamento = agora;
        }
        if (this.dataInicioExecucao == null) {
            this.dataInicioExecucao = agora;
        }
        aprovarOrcamentosVinculados();
        if (this.servicosExecucao.isEmpty()) {
            carregarServicosDosOrcamentosAprovados();
        }
    }

    private void tratarFinalizada(LocalDateTime agora) {
        if (this.dataFimExecucao == null) {
            this.dataFimExecucao = agora;
        }
    }

    private void tratarEntregue(LocalDateTime agora) {
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

    private void tratarCancelada(LocalDateTime agora, String observacao) {
        this.dtEncerramentoOs = agora;
        this.dsMotivoCancelamento = observacao;
        recusarOrcamentosVinculados();
    }

    private void garantirDatasDiagnostico(LocalDateTime agora) {
        if (this.dtInicioDiagnostico == null) {
            this.dtInicioDiagnostico = agora;
        }
        if (this.dtFimDiagnostico == null) {
            this.dtFimDiagnostico = agora;
        }
    }

    public Long getTempoTotalExecucaoMinutos() {
        if (this.dataInicioExecucao != null && this.dataFimExecucao != null) {
            return java.time.Duration.between(
                    this.dataInicioExecucao.atZone(ZoneId.systemDefault()),
                    this.dataFimExecucao.atZone(ZoneId.systemDefault())
            ).toMinutes();
        }
        return null;
    }

    public Integer getTempoTotalEstimadoMinutos() {
        if (this.servicosExecucao == null || this.servicosExecucao.isEmpty()) {
            return 0;
        }
        return this.servicosExecucao.stream()
                .map(OsServico::getServico)
                .filter(Objects::nonNull)
                .map(Servico::getQtTempoEstimadoMin)
                .filter(Objects::nonNull)
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

    private void aprovarOrcamentosVinculados() {
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
            long diasDecorridos = ChronoUnit.DAYS.between(
                    this.dtFimDiagnostico.atZone(ZoneId.systemDefault()),
                    LocalDateTime.now(ZoneId.systemDefault()).atZone(ZoneId.systemDefault())
            );
            if (diasDecorridos > diasLimite) {
                long diasExcedidos = diasDecorridos - diasLimite;
                this.statusOS = StatusOS.CANCELADA;
                this.dtEncerramentoOs = LocalDateTime.now(ZoneId.systemDefault());
                this.dsMotivoCancelamento = "Cancelado automaticamente após " + diasLimite + " dias sem retorno do orçamento (Art. 40 CDC).";
                this.taxaPermanencia = valorDiaria.multiply(BigDecimal.valueOf(diasExcedidos));
                recusarOrcamentosVinculados();
            }
        }
    }

    public void verificarAbandonoTecnico(int diasLimiteAbandono) {
        if (this.statusOS == StatusOS.AGUARDANDO_APROVACAO && this.dtFimDiagnostico != null) {
            long diasDecorridos = ChronoUnit.DAYS.between(
                    this.dtFimDiagnostico.atZone(ZoneId.systemDefault()),
                    LocalDateTime.now(ZoneId.systemDefault()).atZone(ZoneId.systemDefault())
            );
            if (diasDecorridos >= diasLimiteAbandono) {
                this.statusOS = StatusOS.ABANDONADO;
                this.dtEncerramentoOs = LocalDateTime.now(ZoneId.systemDefault());
                this.dsMotivoCancelamento = "Veículo considerado abandonado após " + diasLimiteAbandono + " dias sem manifestação do cliente.";
            }
        }
    }

    public UUID getIdOs() { return idOs; }
    public void setIdOs(UUID idOs) { this.idOs = idOs; }

    public StatusOS getStatusOS() { return statusOS; }
    public void setStatusOS(StatusOS statusOS) { this.statusOS = statusOS; }

    public String getDsRelatoCliente() { return dsRelatoCliente; }
    public void setDsRelatoCliente(String dsRelatoCliente) { this.dsRelatoCliente = dsRelatoCliente; }

    public String getDsDiagnostico() { return dsDiagnostico; }
    public void setDsDiagnostico(String dsDiagnostico) { this.dsDiagnostico = dsDiagnostico; }

    public Boolean getStTermoAceito() { return stTermoAceito; }
    public void setStTermoAceito(Boolean stTermoAceito) { this.stTermoAceito = stTermoAceito; }

    public LocalDateTime getDtAceiteTermo() { return dtAceiteTermo; }
    public void setDtAceiteTermo(LocalDateTime dtAceiteTermo) { this.dtAceiteTermo = dtAceiteTermo; }

    public Integer getNrKmEntrada() { return nrKmEntrada; }
    public void setNrKmEntrada(Integer nrKmEntrada) { this.nrKmEntrada = nrKmEntrada; }

    public LocalDateTime getDtAberturaOs() { return dtAberturaOs; }
    public void setDtAberturaOs(LocalDateTime dtAberturaOs) { this.dtAberturaOs = dtAberturaOs; }

    public LocalDateTime getDtInicioDiagnostico() { return dtInicioDiagnostico; }
    public void setDtInicioDiagnostico(LocalDateTime dtInicioDiagnostico) { this.dtInicioDiagnostico = dtInicioDiagnostico; }

    public LocalDateTime getDtFimDiagnostico() { return dtFimDiagnostico; }
    public void setDtFimDiagnostico(LocalDateTime dtFimDiagnostico) { this.dtFimDiagnostico = dtFimDiagnostico; }

    public LocalDateTime getDtAprovacaoOrcamento() { return dtAprovacaoOrcamento; }
    public void setDtAprovacaoOrcamento(LocalDateTime dtAprovacaoOrcamento) { this.dtAprovacaoOrcamento = dtAprovacaoOrcamento; }

    public LocalDateTime getDataInicioExecucao() { return dataInicioExecucao; }
    public void setDataInicioExecucao(LocalDateTime dataInicioExecucao) { this.dataInicioExecucao = dataInicioExecucao; }

    public LocalDateTime getDataFimExecucao() { return dataFimExecucao; }
    public void setDataFimExecucao(LocalDateTime dataFimExecucao) { this.dataFimExecucao = dataFimExecucao; }

    public LocalDateTime getDtEncerramentoOs() { return dtEncerramentoOs; }
    public void setDtEncerramentoOs(LocalDateTime dtEncerramentoOs) { this.dtEncerramentoOs = dtEncerramentoOs; }

    public LocalDateTime getDtReagendamentoOs() { return dtReagendamentoOs; }
    public void setDtReagendamentoOs(LocalDateTime dtReagendamentoOs) { this.dtReagendamentoOs = dtReagendamentoOs; }

    public StatusPagamento getStPagamento() { return stPagamento; }
    public void setStPagamento(StatusPagamento stPagamento) { this.stPagamento = stPagamento; }

    public String getDsMotivoCancelamento() { return dsMotivoCancelamento; }
    public void setDsMotivoCancelamento(String dsMotivoCancelamento) { this.dsMotivoCancelamento = dsMotivoCancelamento; }

    public BigDecimal getTaxaPermanencia() { return taxaPermanencia; }
    public void setTaxaPermanencia(BigDecimal taxaPermanencia) { this.taxaPermanencia = taxaPermanencia; }

    public UUID getIdCliente() { return idCliente; }
    public void setIdCliente(UUID idCliente) { this.idCliente = idCliente; }

    public UUID getIdVeiculo() { return idVeiculo; }
    public void setIdVeiculo(UUID idVeiculo) { this.idVeiculo = idVeiculo; }

    public UUID getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(UUID idFuncionario) { this.idFuncionario = idFuncionario; }

    public List<Orcamento> getIdsOrcamento() { return idsOrcamento; }
    public void setIdsOrcamento(List<Orcamento> idsOrcamento) { this.idsOrcamento = idsOrcamento; }

    public List<OsServico> getServicosExecucao() { return servicosExecucao; }
    public void setServicosExecucao(List<OsServico> servicosExecucao) { this.servicosExecucao = servicosExecucao; }
}