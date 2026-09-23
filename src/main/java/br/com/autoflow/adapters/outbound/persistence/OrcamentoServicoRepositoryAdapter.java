package br.com.autoflow.adapters.outbound.persistence;

import br.com.autoflow.adapters.outbound.persistence.mapper.OrcamentoServicoEntityMapper;
import br.com.autoflow.adapters.outbound.persistence.repository.SpringDataOrcamentoServicoRepository;
import br.com.autoflow.domain.model.OrcamentoServico;
import br.com.autoflow.ports.outbound.OrcamentoServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrcamentoServicoRepositoryAdapter implements OrcamentoServicoRepositoryPort {

    private final SpringDataOrcamentoServicoRepository repository;
    private final OrcamentoServicoEntityMapper mapper;

    @Override
    public OrcamentoServico save(OrcamentoServico orcamentoServico) {
        var entity = mapper.toEntity(orcamentoServico);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OrcamentoServico> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByServico_IdServico(UUID idServico) {
        return repository.existsByServico_IdServico(idServico);
    }

    @Override
    public void delete(OrcamentoServico orcamentoServico) {
        repository.delete(mapper.toEntity(orcamentoServico));
    }
}