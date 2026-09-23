package br.com.autoflow.adapters.outbound.persistence.entity;

import br.com.autoflow.domain.enums.Genero;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEntity implements Serializable {
    @Id
    @UuidGenerator
    @Column(name = "id_cliente")
    private UUID id;

    @Column(name = "nm_cliente", nullable = false)
    private String nome;

    @Column(name = "nr_documento", nullable = false, unique = true)
    private String documento;

    @Column(name = "nm_email", nullable = false, unique = true)
    private String email;

    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "nr_telefone", nullable = false)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_genero", nullable = false)
    private Genero genero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", nullable = false)
    private EnderecoEntity endereco;
}