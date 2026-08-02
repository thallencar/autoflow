package br.com.autoflow.domain.model;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_enderecos")
@Getter
@Setter
@NoArgsConstructor
public class Endereco {
    @Id
    @UuidGenerator
    @Column(name = "id_endereco")
    private UUID id;

    @Column(name = "nr_cep")
    private String cep;

    @Column(name = "nm_uf")
    private String uf;

    @Column(name = "nm_cidade")
    private String cidade;

    @Column(name = "nm_bairro")
    private String bairro;

    @Column(name = "nm_logradouro")
    private String logradouro;

    @Column(name = "nr_numero")
    private Integer numero;

    @Column(name = "ds_complemento")
    private String complemento;
}
