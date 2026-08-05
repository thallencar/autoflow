package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.repository.EnderecoRepository;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.infrastructure.mapper.EnderecoMapper;
import br.com.autoflow.infrastructure.mapper.FuncionarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuncionarioService {

    private final FuncionarioRepository repository;
    private final EnderecoRepository enderecoRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final EnderecoMapper enderecoMapper;
    private final FuncionarioValidator funcionarioValidator;

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest request) {
        funcionarioValidator.validarParaCriar(request);

        Endereco endereco = enderecoMapper.toEntity(request.endereco());
        endereco = enderecoRepository.save(endereco);

        Funcionario funcionario = funcionarioMapper.toEntity(request, endereco);
        funcionario = repository.save(funcionario);
        return funcionarioMapper.toResponse(funcionario);
    }

    public List<FuncionarioResponse> listar() {
        return repository.findAll()
                .stream()
                .map(funcionarioMapper::toResponse)
                .toList();
    }

    public FuncionarioResponse buscar(UUID id) {
        Funcionario funcionario =
                repository.findById(id)
                        .orElseThrow(() ->
                                new EntidadeNaoEncontradaException("Funcionário", id));
        return funcionarioMapper.toResponse(funcionario);
    }

    @Transactional
    public FuncionarioResponse atualizar(UUID id, FuncionarioRequest request) {

        Funcionario funcionario = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário",id));

        funcionario.atualizarDados(request);
        Funcionario funcionarioAtualizado = repository.save(funcionario);
        return funcionarioMapper.toResponse(funcionarioAtualizado);
    }
    @Transactional
    public void deletar(UUID id) {

        Funcionario funcionario =
                repository.findById(id)
                        .orElseThrow(() ->
                                new EntidadeNaoEncontradaException("Funcionário",id));
        repository.delete(funcionario);
    }
}