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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", nullable = false)
    private Endereco endereco;

    /**
     * Atualiza os dados cadastrais do funcionário e encadeia a atualização do endereço
     */
    public void atualizarDados(FuncionarioRequest request) {
        this.nome = request.nome();
        this.telefone = request.telefone();
        this.email = request.email();
        this.genero = request.genero();
        this.cargo = request.cargo();
        this.dataNascimento = request.dataNascimento();

        if (this.endereco != null && request.endereco() != null) {
            this.endereco.atualizarDados(request.endereco());
        }
    }
}