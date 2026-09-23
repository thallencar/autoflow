package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.VeiculoEntity;
import br.com.autoflow.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataVeiculoRepository extends JpaRepository<VeiculoEntity, UUID> {
    boolean existsByPlaca(String placa);
    Optional<VeiculoEntity> findByPlaca(String placa);
    boolean existsByClienteId(UUID clienteId);
    Optional<VeiculoEntity> findByIdAndClienteId(UUID id, UUID clienteId);
}