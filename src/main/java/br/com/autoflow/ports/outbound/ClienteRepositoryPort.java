package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositoryPort {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(UUID id);
    Optional<Cliente> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);
    List<Cliente> findAll();
    void delete(Cliente cliente);
    boolean existsById(UUID id);
}