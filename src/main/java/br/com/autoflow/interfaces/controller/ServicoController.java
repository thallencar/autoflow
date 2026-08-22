package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.ServicoRequest;
import br.com.autoflow.application.dto.ServicoResponse;
import br.com.autoflow.application.service.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201 Created
    public ServicoResponse criar(@RequestBody @Valid ServicoRequest request) {
        return servicoService.criar(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<ServicoResponse> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "idServico", direction = Sort.Direction.ASC) Pageable pageable) {
        return servicoService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK) // 200 OK
    public ServicoResponse buscarPorId(@PathVariable UUID id) {
        return servicoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid ServicoRequest request) {
        return  servicoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void deletar(@PathVariable UUID id) {
        servicoService.deletar(id);
    }
}