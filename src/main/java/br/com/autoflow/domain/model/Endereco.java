package br.com.autoflow.domain.model;

import java.util.UUID;

public class Endereco {
    private UUID id;
    private String cep;
    private String uf;
    private String cidade;
    private String bairro;
    private String logradouro;
    private Integer numero;
    private String complemento;

    public Endereco() {   }

    public Endereco(UUID id, String cep, String uf, String cidade, String bairro, String logradouro, Integer numero, String complemento) {
        this.id = id;
        this.cep = cep;
        this.uf = uf != null ? uf.toUpperCase() : null;
        this.cidade = cidade;
        this.bairro = bairro;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
    }

    public void atualizar(String cep, String uf, String cidade, String bairro, String logradouro, Integer numero, String complemento) {
        this.cep = cep;
        this.uf = uf != null ? uf.toUpperCase() : null;
        this.cidade = cidade;
        this.bairro = bairro;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
    }

    public UUID getId() { return id; }
    public String getCep() { return cep; }
    public String getUf() { return uf; }
    public String getCidade() { return cidade; }
    public String getBairro() { return bairro; }
    public String getLogradouro() { return logradouro; }
    public Integer getNumero() { return numero; }
    public String getComplemento() { return complemento; }

    public void setId(UUID id) { this.id = id; }
    public void setCep(String cep) { this.cep = cep; }
    public void setUf(String uf) { this.uf = uf != null ? uf.toUpperCase() : null; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
}