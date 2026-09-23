package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ServicoResponse;
import br.com.autoflow.adapters.inbound.mapper.ServicoMapper;
import br.com.autoflow.application.service.ServicoValidator;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.outbound.ServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoUseCase {

    private final ServicoRepositoryPort servicoRepositoryPort;
    private final ServicoMapper servicoMapper;
    private final ServicoValidator servicoValidator;

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        servicoValidator.validarCriacao(request);

        var servico = servicoMapper.toDomain(request);
        var servicoSalvo = servicoRepositoryPort.save(servico);

        return servicoMapper.toResponse(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listarTodos(Pageable pageable) {
        return servicoRepositoryPort.findAll(pageable)
                .map(servicoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(UUID id) {
        Servico servico = servicoValidator.buscarPorId(id);
        return servicoMapper.toResponse(servico);
    }

    @Transactional
    public ServicoResponse atualizar(UUID id, ServicoRequest request) {
        servicoValidator.validarAtualizacao(id, request);

        Servico servico = servicoValidator.buscarPorId(id);
        servicoMapper.updateDomainFromDto(request, servico);

        var servicoAtualizado = servicoRepositoryPort.save(servico);

        return servicoMapper.toResponse(servicoAtualizado);
    }

    @Transactional
    public void deletar(UUID id) {
        servicoValidator.validarExclusao(id);
        servicoRepositoryPort.deleteById(id);
    }
}