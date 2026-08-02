package br.com.autoflow.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import br.com.autoflow.domain.enums.Genero;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    @Id
    @UuidGenerator
    @Column(name = "id_cliente")
    private UUID id;

    @Column(name = "nm_cliente")
    private String nome;

    @Column(name = "nr_documento")
    private String documento;

    @Column(name = "nm_email")
    private String email;

    @Column(name = "dt_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "nr_telefone")
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_genero")
    private Genero genero;

    @ManyToOne
    @JoinColumn(name = "id_endereco")
    private Endereco endereco;
}
