package br.com.autoflow.adapters.inbound.controller;


import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;
import br.com.autoflow.ports.inbound.veiculo.*;
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

    private final CriarVeiculoUseCase criarVeiculoUseCase;
    private final ListarVeiculosUseCase listarVeiculosUseCase;
    private final BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final BuscarVeiculoPorPlacaUseCase buscarVeiculoPorPlacaUseCase;
    private final DeletarVeiculoUseCase deletarVeiculoUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse criar(@RequestBody @Valid VeiculoRequest request) {
        return criarVeiculoUseCase.criar(request);
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return listarVeiculosUseCase.listar();
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscarPorId(@PathVariable UUID id) {
        return buscarVeiculoPorIdUseCase.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable UUID id, @RequestBody @Valid VeiculoRequest request) {
        return atualizarVeiculoUseCase.atualizar(id, request);
    }

    @GetMapping("/placa/{placa}")
    public VeiculoResponse buscarPorPlaca(@PathVariable String placa) {
        return buscarVeiculoPorPlacaUseCase.buscarPorPlaca(placa);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarVeiculoUseCase.deletar(id);
    }
}