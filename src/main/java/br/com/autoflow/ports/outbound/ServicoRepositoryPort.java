package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ServicoRepositoryPort {
    Servico save(Servico servico);
    Page<Servico> findAll(Pageable pageable);
    Optional<Servico> findById(UUID id);
    boolean existsById(UUID id);
    boolean existsByDsServicoIgnoreCase(String dsServico);
    Optional<Servico> findByDsServicoIgnoreCase(String dsServico);
    void deleteById(UUID id);
}