package br.com.autoflow.domain.model;

import br.com.autoflow.application.dto.VeiculoRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_veiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_veiculo")
    private UUID id;

    @Column(name = "nr_placa", nullable = false, unique = true, length = 7)
    private String placa;

    @Column(name = "nm_marca", nullable = false)
    private String marca;

    @Column(name = "nm_modelo", nullable = false)
    private String modelo;

    @Column(name = "km_atual")
    private Integer kmAtual;

    @Column(name = "nr_ano_fabricacao", nullable = false)
    private Short anoFabricacao;

    @Column(name = "nm_cor", nullable = false)
    private String cor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    public void atualizarDados(VeiculoRequest request, Cliente novoCliente) {
        this.placa = request.placa() != null ? request.placa().toUpperCase().trim() : this.placa;
        this.marca = request.marca();
        this.modelo = request.modelo();
        this.anoFabricacao = request.anoFabricacao();
        this.cor = request.cor();
        this.cliente = novoCliente;
    }
}
