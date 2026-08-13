package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.infrastructure.mapper.OrdemServicoMapper;
import br.com.autoflow.domain.entity.OrdemServico;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final OrdemServicoMapper mapper;
    private final OrdemServicoValidator validator;

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarTodas() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));
        return mapper.toResponse(os);
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request, String placaVeiculo, boolean possuiAgendamento) {
        Long carrosNoPatio = repository.countByStOsNot(StatusOS.ENTREGUE);
        validator.validarCriacao(request, placaVeiculo, possuiAgendamento, carrosNoPatio);
        OrdemServico os = mapper.toEntity(request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public OrdemServicoResponse atualizar(UUID id, OrdemServicoRequest request) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));

        validator.validarCliente(request.idCliente());
        validator.validarOrcamentoParaOS(request.idOrcamento());
        mapper.updateEntityFromRequest(os, request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Ordem de Serviço", id);
        }
        repository.deleteById(id);
    }
}