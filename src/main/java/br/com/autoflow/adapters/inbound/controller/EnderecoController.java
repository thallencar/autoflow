package br.com.autoflow.adapters.inbound.controller;

import java.util.List;
import java.util.UUID;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import br.com.autoflow.adapters.inbound.mapper.EnderecoMapper;
import br.com.autoflow.ports.inbound.endereco.*;
import br.com.autoflow.domain.model.Endereco;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final CriarEnderecoUseCase criarEnderecoUseCase;
    private final ListarEnderecosUseCase listarEnderecosUseCase;
    private final BuscarEnderecoPorIdUseCase buscarEnderecoPorIdUseCase;
    private final AtualizarEnderecoUseCase atualizarEnderecoUseCase;
    private final DeletarEnderecoUseCase deletarEnderecoUseCase;

    private final EnderecoMapper enderecoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criar(@RequestBody @Valid EnderecoRequest request) {
        Endereco domain = enderecoMapper.toDomain(request);
        Endereco salvo = criarEnderecoUseCase.criar(domain);
        return enderecoMapper.toResponse(salvo);
    }

    @GetMapping
    public List<EnderecoResponse> listar() {
        return listarEnderecosUseCase.listar().stream()
                .map(enderecoMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EnderecoResponse buscar(@PathVariable UUID id) {
        Endereco endereco = buscarEnderecoPorIdUseCase.buscar(id);
        return enderecoMapper.toResponse(endereco);
    }

    @PutMapping("/{id}")
    public EnderecoResponse atualizar(@PathVariable UUID id, @RequestBody @Valid EnderecoRequest request) {
        Endereco domain = enderecoMapper.toDomain(request);
        Endereco atualizado = atualizarEnderecoUseCase.atualizar(id, domain);
        return enderecoMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarEnderecoUseCase.deletar(id);
    }
}