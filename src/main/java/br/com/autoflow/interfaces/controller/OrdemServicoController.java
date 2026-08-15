package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.AtualizarStatusOSRequest;
import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.application.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService service;

    @GetMapping
    public List<OrdemServicoResponse> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServicoResponse criar(@RequestBody @Valid OrdemServicoRequest request,
                                      @RequestParam(defaultValue = "false") boolean agendamento ) {
        return service.criar(request, agendamento);
    }

    @PutMapping("/{id}")
    public OrdemServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
        return service.atualizar(id, request);
    }
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrdemServicoResponse atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarStatusOSRequest request
    ) {
        return service.atualizarStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletar(id);
    }
}