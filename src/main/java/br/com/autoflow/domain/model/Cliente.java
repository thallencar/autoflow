package br.com.autoflow.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.domain.enums.Genero;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente implements Serializable {
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
    private Endereco endereco;

    /**
     * Atualiza os dados cadastrais do funcionário e encadeia a atualização do endereço
     */
    public void atualizarDados(ClienteRequest request) {
        this.nome = request.nome();
        this.telefone = request.telefone();
        this.email = request.email();
        this.genero = request.genero();
        this.dataNascimento = request.dataNascimento();

        if (this.endereco != null && request.endereco() != null) {
            this.endereco.atualizarDados(request.endereco());
        }
    }
}
