package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Veiculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepositoryPort {
    Veiculo save(Veiculo veiculo);
    List<Veiculo> findAll();
    Optional<Veiculo> findById(UUID id);
    Optional<Veiculo> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
    boolean existsById(UUID id);
    void delete(Veiculo veiculo);
    boolean existsByClienteId(UUID clienteId);
    boolean existsByIdAndClienteId(UUID id, UUID clienteId);
}