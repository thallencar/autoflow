package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioRequest;
import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioResponse;
import br.com.autoflow.application.usecase.FuncionarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioUseCase useCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse criar(@RequestBody @Valid FuncionarioRequest request) {
        return useCase.criar(request);
    }

    @GetMapping
    public List<FuncionarioResponse> listar() {
        return useCase.listar();
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable UUID id) {
        return useCase.buscar(id);
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(@PathVariable UUID id, @RequestBody @Valid FuncionarioRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        useCase.deletar(id);
    }

    @PatchMapping("/{id}/advertencia")
    @ResponseStatus(HttpStatus.OK)
    public String registrarAdvertencia(@PathVariable UUID id) {
        return useCase.registrarAdvertencia(id);
    }
}