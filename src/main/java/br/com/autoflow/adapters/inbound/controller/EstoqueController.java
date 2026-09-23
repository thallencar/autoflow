package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.AdicionarEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.AtualizarValorEstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EstoqueResponse;
import br.com.autoflow.application.usecase.EstoqueUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueUseCase estoqueUseCase;

    @PostMapping
    public ResponseEntity<EstoqueResponse> criar(@RequestBody EstoqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueUseCase.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<EstoqueResponse>> listarTodos() {
        return ResponseEntity.ok(estoqueUseCase.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstoqueResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueUseCase.buscarPorId(id));
    }

    @PatchMapping("/{id}/adicionar-quantidade")
    public ResponseEntity<EstoqueResponse> adicionarQuantidade(@PathVariable UUID id, @RequestBody AdicionarEstoqueRequest request) {
        return ResponseEntity.ok(estoqueUseCase.adicionarQuantidade(id, request));
    }

    @PatchMapping("/{id}/valor-unitario")
    public ResponseEntity<EstoqueResponse> atualizarValorUnitario(@PathVariable UUID id, @RequestBody AtualizarValorEstoqueRequest request) {
        return ResponseEntity.ok(estoqueUseCase.atualizarValorUnitario(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstoqueResponse> atualizar(@PathVariable UUID id, @RequestBody EstoqueRequest request) {
        return ResponseEntity.ok(estoqueUseCase.atualizar(id, request));
    }
}