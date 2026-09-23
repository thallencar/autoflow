package br.com.autoflow.adapters.outbound.persistence.entity;

import br.com.autoflow.domain.enums.Perfil;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "TB_USUARIOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario")
    private UUID id;

    @Column(name = "nm_login", nullable = false, unique = true)
    private String login;

    @Column(name = "nm_senha", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_perfil", nullable = false)
    private Perfil perfil;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private ClienteEntity cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario")
    private FuncionarioEntity funcionario;
}