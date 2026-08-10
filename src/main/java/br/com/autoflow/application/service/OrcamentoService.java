package com.autoflow.application.service;

import com.autoflow.application.dto.OrcamentoRequest;
import com.autoflow.application.dto.OrcamentoResponse;
import com.autoflow.domain.model.Orcamento;
import com.autoflow.domain.repository.OrcamentoRepository;
import com.autoflow.infrastructure.mapper.OrcamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoMapper orcamentoMapper;

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        Orcamento orcamento = orcamentoMapper.toEntity(request);

        // Relaciona os itens com o orçamento pai antes de salvar
        if (orcamento.getItens() != null) {
            orcamento.getItens().forEach(item -> item.setOrcamento(orcamento));
        }

        orcamento = orcamentoRepository.save(orcamento);
        return orcamentoMapper.toResponse(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarTodos() {
        return orcamentoRepository.findAll().stream()
                .map(orcamentoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(UUID id) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
        return orcamentoMapper.toResponse(orcamento);
    }
}