package br.com.autoflow.application.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.EnderecoResponse;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.repository.EnderecoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.infrastructure.mapper.EnderecoMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnderecoService {
    private final  static  String NOME_ENTIDADE = "Endereço";
    private final EnderecoRepository repository;  
    private final EnderecoMapper enderecoMapper;
    private final EnderecoValidator enderecoValidator;
    
    @Transactional
    public EnderecoResponse criar(EnderecoRequest request) {
        enderecoValidator.validarUf(request);

        Endereco endereco = enderecoMapper.toEntity(request);
        endereco = repository.save(endereco);

        return enderecoMapper.toResponse(endereco);
    }
    
    public List<EnderecoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(enderecoMapper::toResponse)
                .toList();
    }

    public EnderecoResponse buscar(UUID id) {
        Endereco endereco =
                repository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        return enderecoMapper.toResponse(endereco);
    }

    @Transactional
    public EnderecoResponse atualizar(UUID id, EnderecoRequest request) {

        Endereco endereco = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        endereco.atualizarDados(request);

        Endereco enderecoAtualizado = repository.save(endereco);

        return enderecoMapper.toResponse(enderecoAtualizado);
    }

    @Transactional
    public void deletar(UUID id) {

        Endereco endereco =
                repository.findById(id)
                        .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        repository.delete(endereco);
    }   
}
