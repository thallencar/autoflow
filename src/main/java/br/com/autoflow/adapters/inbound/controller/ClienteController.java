package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import br.com.autoflow.ports.inbound.cliente.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CriarClienteUseCase criarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final DeletarClienteUseCase deletarClienteUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@RequestBody @Valid ClienteRequest request) {
        return criarClienteUseCase.criar(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ClienteResponse> listar() {
        return listarClientesUseCase.listar();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponse buscarPorId(@PathVariable UUID id) {
        return buscarClientePorIdUseCase.buscarPorId(id);
    }

    @GetMapping("/documento/{documento}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponse buscarPorDocumento(@PathVariable String documento) {
        return buscarClientePorDocumentoUseCase.buscarPorDocumento(documento);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponse atualizar(@PathVariable UUID id, @RequestBody @Valid ClienteUpdateRequest request) {
        return atualizarClienteUseCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarClienteUseCase.deletar(id);
    }
}