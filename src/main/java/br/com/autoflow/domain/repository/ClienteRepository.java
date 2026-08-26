package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);
}
