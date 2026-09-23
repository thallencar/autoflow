package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Genero;
import java.time.LocalDate;
import java.util.UUID;

public class Cliente {
    private UUID id;
    private String nome;
    private String documento;
    private String email;
    private LocalDate dataNascimento;
    private String telefone;
    private Genero genero;
    private Endereco endereco;

    public Cliente() {}

    public Cliente(UUID id, String nome, String documento, String email, LocalDate dataNascimento, String telefone, Genero genero, Endereco endereco) {
        this.id = id;
        this.nome = nome;
        this.documento = documento;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.genero = genero;
        this.endereco = endereco;
    }

    public void atualizarDados(String nome, String telefone, String email, Genero genero, Endereco endereco) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.genero = genero;
        if (endereco != null) {
            this.endereco = endereco;
        }
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
    public String getEmail() { return email; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public Genero getGenero() { return genero; }
    public Endereco getEndereco() { return endereco; }

    public void setId(UUID id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDocumento(String documento) { this.documento = documento; }
    public void setEmail(String email) { this.email = email; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setGenero(Genero genero) { this.genero = genero; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
}