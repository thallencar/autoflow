package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import java.time.LocalDate;
import java.util.UUID;

public class Funcionario {
    private UUID id;
    private String cpf;
    private String nome;
    private String telefone;
    private String email;
    private Genero genero;
    private LocalDate dataNascimento;
    private Cargo cargo;
    private Endereco endereco;
    private boolean ocupado;
    private int nrAdvertencias;

    public Funcionario() {}

    public Funcionario(UUID id, String cpf, String nome, String telefone, String email, Genero genero, LocalDate dataNascimento, Cargo cargo, Endereco endereco, boolean ocupado, int nrAdvertencias) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.cargo = cargo;
        this.endereco = endereco;
        this.ocupado = ocupado;
        this.nrAdvertencias = nrAdvertencias;
    }

    public void ocupar() {
        this.ocupado = true;
    }

    public void liberar() {
        this.ocupado = false;
    }

    public void adicionarAdvertencia() {
        this.nrAdvertencias++;
    }

    public boolean deveSerDemitido() {
        return this.nrAdvertencias >= 3;
    }

    public void atualizar(String nome, String telefone, String email, Genero genero, LocalDate dataNascimento, Cargo cargo, Endereco endereco) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.cargo = cargo;
        this.endereco = endereco;
    }

    public UUID getId() { return id; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public Genero getGenero() { return genero; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public Cargo getCargo() { return cargo; }
    public Endereco getEndereco() { return endereco; }
    public boolean isOcupado() { return ocupado; }
    public int getNrAdvertencias() { return nrAdvertencias; }

    public void setId(UUID id) { this.id = id; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setNome(String nome) { this.nome = nome; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setEmail(String email) { this.email = email; }
    public void setGenero(Genero genero) { this.genero = genero; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
    public void setOcupado(boolean ocupado) { this.ocupado = ocupado; }
    public void setNrAdvertencias(int nrAdvertencias) { this.nrAdvertencias = nrAdvertencias; }
}