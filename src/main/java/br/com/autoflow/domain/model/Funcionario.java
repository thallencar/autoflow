package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "TB_FUNCIONARIOS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario  implements Serializable {

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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", nullable = false)
    private Endereco endereco;

    @Column(name = "st_ocupado")
    private boolean ocupado = false;

    @Column(name = "nr_advertencias")
    private int nr_advertencias = 0;

    public void ocupar() {
        this.ocupado = true;
    }

    public void liberar() {
        this.ocupado = false;
    }

    public void adicionarAdvertencia() {
        this.nr_advertencias++;
    }

    public boolean deveSerDemitido() {
        return this.nr_advertencias >= 3;
    }
}