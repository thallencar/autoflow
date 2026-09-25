package br.com.autoflow.domain.model;

import java.util.UUID;

public class Veiculo {
    private UUID id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer kmAtual;
    private Short anoFabricacao;
    private String cor;
    private UUID clienteId;

    public Veiculo() {  }

    public Veiculo(UUID id, String placa, String marca, String modelo, Integer kmAtual, Short anoFabricacao, String cor, UUID clienteId) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.kmAtual = kmAtual;
        this.anoFabricacao = anoFabricacao;
        this.cor = cor;
        this.clienteId = clienteId;
    }

    public void atualizar(String placa, String marca, String modelo, Integer kmAtual, Short anoFabricacao, String cor) {
        if (placa != null && !placa.isBlank()) {
            this.placa = placa.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        }
        this.marca = marca;
        this.modelo = modelo;
        this.kmAtual = kmAtual;
        this.anoFabricacao = anoFabricacao;
        this.cor = cor;
    }

    public UUID getId() { return id; }
    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public Integer getKmAtual() { return kmAtual; }
    public Short getAnoFabricacao() { return anoFabricacao; }
    public String getCor() { return cor; }
    public UUID getClienteId() { return clienteId; }

    public void setId(UUID id) { this.id = id; }
    public void setPlaca(String placa) { this.placa = placa; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setKmAtual(Integer kmAtual) { this.kmAtual = kmAtual; }
    public void setAnoFabricacao(Short anoFabricacao) { this.anoFabricacao = anoFabricacao; }
    public void setCor(String cor) { this.cor = cor; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
}