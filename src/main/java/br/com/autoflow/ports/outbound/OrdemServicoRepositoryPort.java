package br.com.autoflow.ports.outbound;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.model.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepositoryPort {
    Page<OrdemServico> findAll(Pageable pageable);
    Page<OrdemServico> findByStatusOS(StatusOS status, Pageable pageable);
    Optional<OrdemServico> findById(UUID id);
    OrdemServico save(OrdemServico ordemServico);
    void deleteById(UUID id);
    Long countByStatusOSNotIn(List<StatusOS> status);
    boolean existsByIdVeiculoAndStatusOSNotIn(UUID idVeiculo, List<StatusOS> status);
    Optional<OrdemServico> findTopByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo);
    Page<OrdemServico> findByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo, Pageable pageable);
    Page<OrdemServico> findMetricasComFiltro(LocalDateTime dataInicio, LocalDateTime dataFim, StatusOS status, Pageable pageable);
    boolean existsByIdVeiculo(UUID idVeiculo);
}