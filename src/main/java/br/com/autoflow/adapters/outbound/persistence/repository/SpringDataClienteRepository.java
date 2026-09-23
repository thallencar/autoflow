package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataClienteRepository extends JpaRepository<ClienteEntity, UUID> {
    Optional<ClienteEntity> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);
}