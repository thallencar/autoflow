package br.com.autoflow.interfaces.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.application.dto.EnderecoResponse;
import br.com.autoflow.application.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
public class EnderecoController {
    private final EnderecoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criar(@RequestBody @Valid EnderecoRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<EnderecoResponse> listar(){
        return service.listar();
    }

    @GetMapping("/{id}")
    public EnderecoResponse buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public EnderecoResponse atualizar(@PathVariable UUID id, @RequestBody @Valid EnderecoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletar(id);
    }
}
