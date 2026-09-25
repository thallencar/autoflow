package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Endereco;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnderecoRepositoryPort {
    Endereco save(Endereco endereco);
    List<Endereco> findAll();
    Optional<Endereco> findById(UUID id);
    void delete(Endereco endereco);
    Optional<Endereco> findByCepAndNumero(String cep, Integer numero);
}