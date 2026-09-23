package br.com.autoflow.application.usecase;

import java.util.List;
import java.util.UUID;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autoflow.application.validator.EnderecoValidator;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.adapters.inbound.mapper.EnderecoMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnderecoUseCase {
    private static final String NOME_ENTIDADE = "Endereço";

    private final EnderecoRepositoryPort repositoryPort;
    private final EnderecoMapper enderecoMapper;
    private final EnderecoValidator enderecoValidator;

    @Transactional
    public EnderecoResponse criar(EnderecoRequest request) {
        enderecoValidator.validarUf(request);

        Endereco endereco = enderecoMapper.toDomain(request);
        endereco = repositoryPort.save(endereco);

        return enderecoMapper.toResponse(endereco);
    }

    public List<EnderecoResponse> listar() {
        return repositoryPort.findAll().stream()
                .map(enderecoMapper::toResponse)
                .toList();
    }

    public EnderecoResponse buscar(UUID id) {
        Endereco endereco = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        return enderecoMapper.toResponse(endereco);
    }

    @Transactional
    public EnderecoResponse atualizar(UUID id, EnderecoRequest request) {
        Endereco endereco = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        enderecoMapper.updateDomainFromDto(request, endereco);
        Endereco enderecoAtualizado = repositoryPort.save(endereco);

        return enderecoMapper.toResponse(enderecoAtualizado);
    }

    @Transactional
    public void deletar(UUID id) {
        Endereco endereco = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        repositoryPort.delete(endereco);
    }
}