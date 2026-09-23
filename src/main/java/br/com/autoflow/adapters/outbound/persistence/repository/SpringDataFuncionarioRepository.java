package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFuncionarioRepository extends JpaRepository<FuncionarioEntity, UUID> {
    Optional<FuncionarioEntity> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    Optional<FuncionarioEntity> findByEmail(String email);
}