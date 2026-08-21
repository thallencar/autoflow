package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.application.service.FuncionarioService;
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

    private final FuncionarioService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse criar(@RequestBody @Valid FuncionarioRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<FuncionarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(@PathVariable UUID id, @RequestBody @Valid FuncionarioRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletarFuncionario(id);
    }

    @PatchMapping("/{id}/advertencia")
    @ResponseStatus(HttpStatus.OK)
    public String registrarAdvertencia(@PathVariable UUID id) {
       return service.registrarAdvertencia(id);
    }
}