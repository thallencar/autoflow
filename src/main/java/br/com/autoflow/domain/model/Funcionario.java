package br.com.autoflow.domain.model;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "TB_FUNCIONARIOS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    @UuidGenerator
    @Column(name = "id_funcionario")
    private UUID idFuncionario;

    @Column(name = "nr_cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "nm_funcionario", nullable = false)
    private String nome;

    @Column(name = "nr_telefone", nullable = false)
    private String telefone;

    @Column(name = "nm_email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_genero", nullable = false)
    private Genero genero;

    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "ds_cargo", nullable = false)
    private Cargo cargo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", nullable = false)
    private Endereco endereco;

    public void atualizarDados(String nome,
                               String telefone,
                               String email,
                               Genero genero,
                               Cargo cargo,
                               LocalDate dataNascimento) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.genero = genero;
        this.cargo = cargo;
        this.dataNascimento = dataNascimento;
    }
}