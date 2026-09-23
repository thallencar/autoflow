package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Estoque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueRepositoryPort {
    Estoque save(Estoque estoque);
    List<Estoque> findAll();
    Optional<Estoque> findById(UUID id);
    List<Estoque> findByNomeItemContainingIgnoreCase(String nome);
}