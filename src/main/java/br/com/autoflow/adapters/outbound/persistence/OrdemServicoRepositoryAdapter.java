package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.adapters.outbound.persistence.mapper.OrdemServicoEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataOrdemServicoRepository;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.model.OrdemServico;

import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepositoryPort {

    private final SpringDataOrdemServicoRepository springDataRepository;
    private final OrdemServicoEntityMapper mapper;

    @Override
    public Page<OrdemServico> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<OrdemServico> findByStatusOS(StatusOS status, Pageable pageable) {
        return springDataRepository.findByStatusOS(status, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<OrdemServico> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public OrdemServico save(OrdemServico ordemServico) {
        OrdemServicoEntity entity = mapper.toEntity(ordemServico);
        OrdemServicoEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public Long countByStatusOSNotIn(List<StatusOS> status) {
        return springDataRepository.countByStatusOSNotIn(status);
    }

    @Override
    public boolean existsByIdVeiculoAndStatusOSNotIn(UUID idVeiculo, List<StatusOS> status) {
        return springDataRepository.existsByIdVeiculoAndStatusOSNotIn(idVeiculo, status);
    }

    @Override
    public Optional<OrdemServico> findTopByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo) {
        return springDataRepository.findTopByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo).map(mapper::toDomain);
    }

    @Override
    public Page<OrdemServico> findByIdVeiculoOrderByDtAberturaOsDesc(UUID idVeiculo, Pageable pageable) {
        return springDataRepository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<OrdemServico> findMetricasComFiltro(LocalDateTime dataInicio, LocalDateTime dataFim, StatusOS status, Pageable pageable) {
        return springDataRepository.findMetricasComFiltro(dataInicio, dataFim, status, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existsByIdVeiculo(UUID idVeiculo) {
        return springDataRepository.existsByIdVeiculo(idVeiculo);
    }
}