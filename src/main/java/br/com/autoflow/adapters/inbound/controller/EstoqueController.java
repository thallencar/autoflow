package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AdicionarEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueResponse;
import br.com.autoflow.adapters.inbound.mapper.EstoqueMapper;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.ports.inbound.estoque.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final CriarEstoqueUseCase criarEstoqueUseCase;
    private final ListarEstoqueUseCase listarEstoqueUseCase;
    private final BuscarEstoquePorIdUseCase buscarEstoquePorIdUseCase;
    private final AtualizarEstoqueUseCase atualizarEstoqueUseCase;

    private final EstoqueMapper estoqueMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstoqueResponse criar(@RequestBody @Valid EstoqueRequest request) {
        Estoque domain = estoqueMapper.toDomain(request);
        Estoque salvo = criarEstoqueUseCase.criar(domain);
        return estoqueMapper.toResponse(salvo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EstoqueResponse> listarTodos() {
        return listarEstoqueUseCase.listarTodos().stream()
                .map(estoqueMapper::toResponse)
                .toList();
    }

    @GetMapping("/estoque-baixo")
    @ResponseStatus(HttpStatus.OK)
    public List<EstoqueResponse> listarInsumosComEstoqueBaixo() {
        return listarEstoqueUseCase.listarInsumosComEstoqueBaixo().stream()
                .map(estoqueMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EstoqueResponse buscarPorId(@PathVariable UUID id) {
        Estoque estoque = buscarEstoquePorIdUseCase.buscarPorId(id);
        return estoqueMapper.toResponse(estoque);
    }

    @PatchMapping("/{id}/adicionar-quantidade")
    @ResponseStatus(HttpStatus.OK)
    public EstoqueResponse adicionarQuantidade(@PathVariable UUID id, @RequestBody @Valid AdicionarEstoqueRequest request) {
        Estoque atualizado = atualizarEstoqueUseCase.adicionarQuantidade(id, request.quantidade());
        return estoqueMapper.toResponse(atualizado);
    }

    @PatchMapping("/{id}/valor-unitario")
    @ResponseStatus(HttpStatus.OK)
    public EstoqueResponse atualizarValorUnitario(@PathVariable UUID id, @RequestBody @Valid AtualizarValorEstoqueRequest request) {
        Estoque atualizado = atualizarEstoqueUseCase.atualizarValorUnitario(id, request.valorUnitario());
        return estoqueMapper.toResponse(atualizado);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EstoqueResponse atualizar(@PathVariable UUID id, @RequestBody @Valid EstoqueRequest request) {
        Estoque domain = estoqueMapper.toDomain(request);
        Estoque atualizado = atualizarEstoqueUseCase.atualizar(id, domain);
        return estoqueMapper.toResponse(atualizado);
    }
}