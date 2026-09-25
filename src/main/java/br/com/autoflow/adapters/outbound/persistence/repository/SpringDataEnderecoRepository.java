package br.com.autoflow.adapters.outbound.persistence.repository;

import br.com.autoflow.adapters.outbound.persistence.entity.EnderecoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataEnderecoRepository extends JpaRepository<EnderecoEntity, UUID> {
    Optional<EnderecoEntity> findByCepAndNumero(String cep, Integer numero);
}