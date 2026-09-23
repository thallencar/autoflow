package br.com.autoflow.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Servico {
    private UUID idServico;
    private String dsServico;
    private BigDecimal vlServico;
    private Integer qtTempoEstimadoMin;

    public Servico(UUID idServico, String dsServico, BigDecimal vlServico, Integer qtTempoEstimadoMin) {
        this.idServico = idServico;
        this.dsServico = dsServico;
        this.vlServico = vlServico;
        this.qtTempoEstimadoMin = qtTempoEstimadoMin;
    }

    public void atualizar(String dsServico, BigDecimal vlServico, Integer qtTempoEstimadoMin) {
        this.dsServico = dsServico;
        this.vlServico = vlServico;
        this.qtTempoEstimadoMin = qtTempoEstimadoMin;
    }

    public UUID getIdServico() { return idServico; }
    public String getDsServico() { return dsServico; }
    public BigDecimal getVlServico() { return vlServico; }
    public Integer getQtTempoEstimadoMin() { return qtTempoEstimadoMin; }

    public void setIdServico(UUID idServico) { this.idServico = idServico; }
    public void setDsServico(String dsServico) { this.dsServico = dsServico; }
    public void setVlServico(BigDecimal vlServico) { this.vlServico = vlServico; }
    public void setQtTempoEstimadoMin(Integer qtTempoEstimadoMin) { this.qtTempoEstimadoMin = qtTempoEstimadoMin; }
}