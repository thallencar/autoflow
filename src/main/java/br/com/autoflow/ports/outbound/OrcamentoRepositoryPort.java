package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.model.Orcamento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepositoryPort {
    Orcamento save(Orcamento orcamento);
    Orcamento saveAndFlush(Orcamento orcamento);
    Optional<Orcamento> findById(UUID id);
    List<Orcamento> findAll();
    List<Orcamento> findByOrdemServicoIdOs(UUID idOs);
    boolean existsById(UUID id);
    boolean existsByIdAndOrdemServicoIsNotNull(UUID idOrcamento);
    void deletarItensDiretosPorOrcamento(UUID id);
    void deletarItensPorServicosDoOrcamento(UUID id);
    void deletarServicosPorOrcamento(UUID id);
}