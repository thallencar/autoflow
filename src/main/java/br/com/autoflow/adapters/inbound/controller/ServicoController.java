package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ServicoResponse;
import br.com.autoflow.application.usecase.ServicoUseCase;
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

    private final ServicoUseCase servicoUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse criar(@RequestBody @Valid ServicoRequest request) {
        return servicoUseCase.criar(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<ServicoResponse> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "idServico", direction = Sort.Direction.ASC) Pageable pageable) {
        return servicoUseCase.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ServicoResponse buscarPorId(@PathVariable UUID id) {
        return servicoUseCase.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid ServicoRequest request) {
        return servicoUseCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        servicoUseCase.deletar(id);
    }
}