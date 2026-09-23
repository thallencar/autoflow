package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Perfil;
import java.util.UUID;

public class Usuario {
    private UUID id;
    private String login;
    private String senha;
    private Perfil perfil;
    private Cliente cliente;
    private Funcionario funcionario;

    public Usuario() {  }

    public Usuario(UUID id, String login, String senha, Perfil perfil, Cliente cliente, Funcionario funcionario) {
        this.id = id;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
        this.cliente = cliente;
        this.funcionario = funcionario;
    }

    /**
     * Factory Method de Domínio (recebe a senha já tratada/criptografada pela aplicação).
     */
    public static Usuario criarUsuarioParaFuncionario(Funcionario funcionario, Perfil perfil, String senhaCriptografada) {
        return new Usuario(
                null,
                funcionario.getEmail(),
                senhaCriptografada,
                perfil,
                null,
                funcionario
        );
    }

    public static Usuario criarUsuarioParaCliente(Cliente cliente, Perfil perfil, String senhaCriptografada) {
        return new Usuario(
                null,
                cliente.getEmail(),
                senhaCriptografada,
                perfil,
                cliente,
                null
        );
    }

    public void atualizarDadosAcesso(String novoEmail, Perfil novoPerfil) {
        this.login = novoEmail;
        this.perfil = novoPerfil;
    }

    public UUID getId() { return id; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public Perfil getPerfil() { return perfil; }
    public Cliente getCliente() { return cliente; }
    public Funcionario getFuncionario() { return funcionario; }

    public void setId(UUID id) { this.id = id; }
    public void setLogin(String login) { this.login = login; }
    public void setSenha(String senha) { this.senha = senha; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
}