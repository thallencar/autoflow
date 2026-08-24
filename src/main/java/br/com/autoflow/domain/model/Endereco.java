package br.com.autoflow.domain.model;

import java.util.UUID;

import br.com.autoflow.application.dto.EnderecoRequest;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TB_ENDERECOS")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Endereco {

    @Id
    @UuidGenerator
    @Column(name = "id_endereco")
    private UUID id;

    @Column(name = "nr_cep", nullable = false)
    private String cep;

    @Column(name = "nm_uf", nullable = false)
    private String uf;

    @Column(name = "nm_cidade", nullable = false)
    private String cidade;

    @Column(name = "nm_bairro", nullable = false)
    private String bairro;

    @Column(name = "nm_logradouro", nullable = false)
    private String logradouro;

    @Column(name = "nr_numero", nullable = false)
    private Integer numero;

    @Column(name = "ds_complemento")
    private String complemento;
}