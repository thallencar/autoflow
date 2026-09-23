package br.com.autoflow.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "TB_ENDERECOS")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoEntity implements Serializable {

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