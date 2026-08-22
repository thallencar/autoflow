package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    boolean existsByPlaca(String placa);
    Optional<Veiculo> findByPlaca(String placa);
    boolean existsByIdAndClienteId(UUID idCliente, UUID veiculoId);
}
