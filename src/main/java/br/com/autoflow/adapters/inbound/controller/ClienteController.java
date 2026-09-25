package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import br.com.autoflow.adapters.inbound.mapper.ClienteMapper;
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
    private final ClienteMapper clienteMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@RequestBody @Valid ClienteRequest request) {
        var enderecoDomain = clienteMapper.toEnderecoDomain(request.endereco());
        var clienteDomain = clienteMapper.toDomain(request, enderecoDomain);

        var clienteSalvo = criarClienteUseCase.criar(clienteDomain);
        return clienteMapper.toResponse(clienteSalvo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ClienteResponse> listar() {
        return listarClientesUseCase.listar().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponse buscarPorId(@PathVariable UUID id) {
        var cliente = buscarClientePorIdUseCase.buscarPorId(id);
        return clienteMapper.toResponse(cliente);
    }

    @GetMapping("/documento/{documento}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponse buscarPorDocumento(@PathVariable String documento) {
        var cliente = buscarClientePorDocumentoUseCase.buscarPorDocumento(documento);
        return clienteMapper.toResponse(cliente);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClienteResponse atualizar(@PathVariable UUID id, @RequestBody @Valid ClienteUpdateRequest request) {
        var enderecoDomain = request.endereco() != null ? clienteMapper.toEnderecoDomain(request.endereco()) : null;
        var clienteDomain = clienteMapper.toDomain(request, enderecoDomain);

        var clienteAtualizado = atualizarClienteUseCase.atualizar(id, clienteDomain);
        return clienteMapper.toResponse(clienteAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarClienteUseCase.deletar(id);
    }
}