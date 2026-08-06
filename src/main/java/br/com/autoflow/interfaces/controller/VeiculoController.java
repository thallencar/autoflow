package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.application.dto.VeiculoResponse;
import br.com.autoflow.application.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse criar(@RequestBody @Valid VeiculoRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid VeiculoRequest request) {

        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletar(id);
    }
}
