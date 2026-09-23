package br.com.autoflow.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OsServico {
    private UUID id;
    private OrdemServico ordemServico;
    private Servico servico;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;

    public OsServico() {
    }

    public OsServico(UUID id, OrdemServico ordemServico, Servico servico, LocalDateTime dataInicioExecucao, LocalDateTime dataFimExecucao) {
        this.id = id;
        this.ordemServico = ordemServico;
        this.servico = servico;
        this.dataInicioExecucao = dataInicioExecucao;
        this.dataFimExecucao = dataFimExecucao;
    }

    public void iniciarExecucao(LocalDateTime dataInicio) {
        this.dataInicioExecucao = dataInicio;
    }

    public void finalizarExecucao(LocalDateTime dataFim) {
        this.dataFimExecucao = dataFim;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public LocalDateTime getDataInicioExecucao() { return dataInicioExecucao; }
    public void setDataInicioExecucao(LocalDateTime dataInicioExecucao) { this.dataInicioExecucao = dataInicioExecucao; }

    public LocalDateTime getDataFimExecucao() { return dataFimExecucao; }
    public void setDataFimExecucao(LocalDateTime dataFimExecucao) { this.dataFimExecucao = dataFimExecucao; }
}