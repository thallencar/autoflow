package com.autoflow.interfaces.controller;

import com.autoflow.application.dto.EstoqueRequest;
import com.autoflow.application.dto.EstoqueResponse;
import com.autoflow.application.service.EstoqueService;
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

    private final EstoqueService estoqueService;

    @PostMapping
    public ResponseEntity<EstoqueResponse> criar(@RequestBody EstoqueRequest request) {
        EstoqueResponse response = estoqueService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EstoqueResponse>> listarTodos() {
        return ResponseEntity.ok(estoqueService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstoqueResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.buscarPorId(id));
    }

    @PatchMapping("/{id}/adicionar-quantidade")
    public ResponseEntity<EstoqueResponse> adicionarQuantidade(
            @PathVariable UUID id,
            @RequestBody AdicionarEstoqueRequest request) {
        return ResponseEntity.ok(estoqueService.adicionarQuantidade(id, request));
    }

    @PatchMapping("/{id}/valor-unitario")
    public ResponseEntity<EstoqueResponse> atualizarValorUnitario(
            @PathVariable UUID id,
            @RequestBody AtualizarValorEstoqueRequest request) {
        return ResponseEntity.ok(estoqueService.atualizarValorUnitario(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstoqueResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody EstoqueRequest request) {
        return ResponseEntity.ok(estoqueService.atualizar(id, request));
    }
}