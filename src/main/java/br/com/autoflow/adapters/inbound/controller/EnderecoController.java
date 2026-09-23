package br.com.autoflow.adapters.inbound.controller;

import java.util.List;
import java.util.UUID;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import br.com.autoflow.application.usecase.EnderecoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
public class EnderecoController {
    private final EnderecoUseCase useCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criar(@RequestBody @Valid EnderecoRequest request) {
        return useCase.criar(request);
    }

    @GetMapping
    public List<EnderecoResponse> listar(){
        return useCase.listar();
    }

    @GetMapping("/{id}")
    public EnderecoResponse buscar(@PathVariable UUID id) {
        return useCase.buscar(id);
    }

    @PutMapping("/{id}")
    public EnderecoResponse atualizar(@PathVariable UUID id, @RequestBody @Valid EnderecoRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        useCase.deletar(id);
    }
}