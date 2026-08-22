package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.infrastructure.mapper.OrcamentoMapper;
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
    private final EstoqueService estoqueService;

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        if (request.itens() != null && !request.itens().isEmpty()) {
            estoqueService.reservarEstoqueParaItens(request.itens());
        }

        Orcamento orcamento = orcamentoMapper.toEntity(request);

        if (orcamento.getItens() != null) {
            for (var item : orcamento.getItens()) {
                item.setOrcamento(orcamento);
            }
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

    @Transactional
    public OrcamentoResponse rejeitar(UUID id) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));

        if ("Rejeitado".equalsIgnoreCase(orcamento.getStatus())) {
            throw new IllegalStateException("Este orçamento já está rejeitado.");
        }

        if (orcamento.getItens() != null && !orcamento.getItens().isEmpty()) {
            List<br.com.autoflow.application.dto.OrcamentoItemRequest> itensRequest = orcamento.getItens().stream()
                    .map(item -> new br.com.autoflow.application.dto.OrcamentoItemRequest(
                            item.getQuantidade(),
                            item.getValorUnitario(),
                            item.getValorTotal(),
                            item.getIdEstoque()
                    ))
                    .collect(Collectors.toList());

            estoqueService.devolverEstoqueDeItens(itensRequest);
        }

        orcamento.setStatus("Rejeitado");
        orcamento.setDataDecisao(java.time.LocalDateTime.now());

        orcamento = orcamentoRepository.save(orcamento);
        return orcamentoMapper.toResponse(orcamento);
    }

    @Transactional
    public OrcamentoResponse cancelarPorExpiracao(UUID id) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));

        if ("Cancelado".equalsIgnoreCase(orcamento.getStatus()) || "Rejeitado".equalsIgnoreCase(orcamento.getStatus())) {
            return orcamentoMapper.toResponse(orcamento);
        }

        if (java.time.LocalDateTime.now().isAfter(orcamento.getDataExpiracao())) {
            if (orcamento.getItens() != null && !orcamento.getItens().isEmpty()) {
                List<br.com.autoflow.application.dto.OrcamentoItemRequest> itensRequest = orcamento.getItens().stream()
                        .map(item -> new br.com.autoflow.application.dto.OrcamentoItemRequest(
                                item.getQuantidade(),
                                item.getValorUnitario(),
                                item.getValorTotal(),
                                item.getIdEstoque()
                        ))
                        .collect(Collectors.toList());

                estoqueService.devolverEstoqueDeItens(itensRequest);
            }

            orcamento.setStatus("Cancelado");
            orcamento.setDataDecisao(java.time.LocalDateTime.now());
            orcamento = orcamentoRepository.save(orcamento);
        }

        return orcamentoMapper.toResponse(orcamento);
    }
}