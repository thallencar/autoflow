package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.entity.OrdemServicoEntity;
import br.com.autoflow.adapters.outbound.persistence.mapper.OrcamentoEntityMapper;

import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataOrcamentoRepository;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.model.Orcamento;

import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrcamentoRepositoryAdapter implements OrcamentoRepositoryPort {

    private final SpringDataOrcamentoRepository repository;
    private final OrcamentoEntityMapper mapper;
    private final EntityManager entityManager;

    @Override
    public Orcamento save(Orcamento orcamento) {
        var entity = mapper.toEntity(orcamento);
        if (orcamento.getId() == null) {
            entity.setId(null);

            if (entity.getItens() != null) {
                entity.getItens().forEach(item -> item.setId(null));
            }
        }
        if (entity.getOrdemServico() != null && entity.getOrdemServico().getIdOs() != null) {
            var managedOs = entityManager.getReference(OrdemServicoEntity.class, entity.getOrdemServico().getIdOs());
            entity.setOrdemServico(managedOs);
        }
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Orcamento saveAndFlush(Orcamento orcamento) {
        var entity = mapper.toEntity(orcamento);
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Orcamento> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Orcamento> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Orcamento> findByOrdemServicoIdOs(UUID idOs) {
        return repository.findByOrdemServicoIdOs(idOs).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByIdAndOrdemServicoIsNotNull(UUID idOrcamento) {
        return repository.existsByIdAndOrdemServicoIsNotNull(idOrcamento);
    }

    @Override
    public void deletarItensDiretosPorOrcamento(UUID id) {
        repository.deletarItensDiretosPorOrcamento(id);
    }

    @Override
    public void deletarItensPorServicosDoOrcamento(UUID id) {
        repository.deletarItensPorServicosDoOrcamento(id);
    }

    @Override
    public void deletarServicosPorOrcamento(UUID id) {
        repository.deletarServicosPorOrcamento(id);
    }

    @Override
    public void atualizarStatusOrcamento(UUID id, StatusOrcamento status) {
        repository.atualizarStatusOrcamento(id, status);
    }
}