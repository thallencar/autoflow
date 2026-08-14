package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Perfil;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "TB_USUARIOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario")
    private UUID id;

    @Column(name = "nm_login", nullable = false, unique = true )
    private String login;

    @Column(name = "nm_senha", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_perfil", nullable = false)
    private Perfil perfil;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario")
    private Funcionario funcionario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.perfil == Perfil.ADMIN){
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_MECANICO"),
                    new SimpleGrantedAuthority("ROLE_CLIENTE")
            );
        } else if (this.perfil == Perfil.MECANICO) {
            return List.of(new SimpleGrantedAuthority("ROLE_MECANICO"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    @Override
    public String getPassword() { return this.senha; }

    @Override
    public String getUsername() { return this.login; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /**
     * Factory Method: Encapsula a criação de um usuário vinculado a um funcionário.
     */
    public static Usuario criarUsuarioParaFuncionario(Funcionario funcionario, Perfil perfil, PasswordEncoder passwordEncoder) {
        return Usuario.builder()
                .login(funcionario.getEmail())
                .senha(passwordEncoder.encode(funcionario.getCpf()))
                .perfil(perfil)
                .funcionario(funcionario)
                .build();
    }

    public static Usuario criarUsuarioParaCliente(Cliente cliente, Perfil perfil, PasswordEncoder passwordEncoder) {
        return Usuario.builder()
            .login(cliente.getEmail())
            .senha(passwordEncoder.encode(cliente.getDocumento()))
            .perfil(perfil)
            .cliente(cliente)
            .build();
    }

    /**
     * Atualiza dados de acesso com base nas mudanças do funcionário.
     */
    public void atualizarDadosAcesso(String novoEmail, Perfil novoPerfil) {
        this.login = novoEmail;
        this.perfil = novoPerfil;
    }
}
