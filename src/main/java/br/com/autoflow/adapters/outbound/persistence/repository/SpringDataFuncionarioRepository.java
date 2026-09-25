package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFuncionarioRepository extends JpaRepository<FuncionarioEntity, UUID> {

    Optional<FuncionarioEntity> findByCpf(String cpf);

    Optional<FuncionarioEntity> findByEmail(String email);

    @Query("SELECT COUNT(f) > 0 FROM FuncionarioEntity f WHERE f.cpf = :cpf")
    boolean existsByCpf(@Param("cpf") String cpf);

    @Query("SELECT COUNT(f) > 0 FROM FuncionarioEntity f WHERE f.email = :email")
    boolean existsByEmail(@Param("email") String email);
}