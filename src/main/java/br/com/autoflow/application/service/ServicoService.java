package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.ServicoRequest;
import br.com.autoflow.application.dto.ServicoResponse;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.infrastructure.mapper.ServicoMapper;
import br.com.autoflow.domain.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;
    private final ServicoValidator servicoValidator;

   @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        servicoValidator.validarCriacao(request);

        var entity = servicoMapper.toEntity(request);
        var entitySalva = servicoRepository.save(entity);

        return servicoMapper.toResponse(entitySalva);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listarTodos(Pageable pageable) {
        return servicoRepository.findAll(pageable)
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

        var entity = servicoRepository.getReferenceById(id);
        servicoMapper.updateEntityFromDto(request, entity);
        var entityAtualizada = servicoRepository.save(entity);

        return servicoMapper.toResponse(entityAtualizada);
    }

    @Transactional
    public void deletar(UUID id) {
        servicoValidator.validarExclusao(id);
        servicoRepository.deleteById(id);
    }
}