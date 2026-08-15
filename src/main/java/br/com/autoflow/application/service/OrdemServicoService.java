package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarStatusOSRequest;
import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrdemServicoMapper;
import br.com.autoflow.domain.model.OrdemServico;
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
        List<OrdemServico> lista = repository.findAll();
        return mapper.toResponseList(lista);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));
        return mapper.toResponse(os);
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request, boolean possuiAgendamento) {
        Long carrosNoPatio = repository.countByStatusOSNot(StatusOS.ENTREGUE);
        validator.validarCriacao(request, possuiAgendamento, carrosNoPatio);
        OrdemServico os = mapper.toEntity(request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public OrdemServicoResponse atualizar(UUID id, OrdemServicoRequest request) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));

        validator.validarCliente(request.idCliente());
        validator.validarOrcamentosParaOS(request.idsOrcamento());
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
    @Transactional
    public OrdemServicoResponse atualizarStatus(UUID idOS, AtualizarStatusOSRequest request) {
        OrdemServico os = repository.findById(idOS)
                .orElseThrow(() -> new RegraNegocioException("Ordem de Serviço não encontrada."));
        os.atualizarStatus(request.status(), request.observacao());
        OrdemServico osSalva = repository.save(os);
        OrdemServicoResponse response = mapper.toResponse(osSalva);
        return response;
    }
}