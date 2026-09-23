package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByLogin(String login);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.funcionario.id = :idFuncionario")
    Optional<UsuarioEntity> findByFuncionario_Id(@Param("idFuncionario") UUID idFuncionario);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.cliente.id = :idCliente")
    Optional<UsuarioEntity> findByCliente_Id(@Param("idCliente") UUID idCliente);
}