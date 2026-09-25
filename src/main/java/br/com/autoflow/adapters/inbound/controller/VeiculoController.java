package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;
import br.com.autoflow.adapters.inbound.mapper.VeiculoMapper;
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
    private final VeiculoMapper veiculoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse criar(@RequestBody @Valid VeiculoRequest request) {
        // Converte o Request (DTO) para o modelo de Domínio
        var veiculoDomain = veiculoMapper.toDomain(request);

        // Passa o Domínio para o UseCase (que agora trabalha exclusivamente com Domínio)
        var salvo = criarVeiculoUseCase.criar(veiculoDomain);

        // Converte o Domínio de volta para Response (DTO)
        return veiculoMapper.toResponse(salvo);
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        // O UseCase retorna uma lista de Domínios, mapeamos cada um para Response
        return listarVeiculosUseCase.listar().stream()
                .map(veiculoMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscarPorId(@PathVariable UUID id) {
        var veiculo = buscarVeiculoPorIdUseCase.buscarPorId(id);
        return veiculoMapper.toResponse(veiculo);
    }

    @GetMapping("/placa/{placa}")
    public VeiculoResponse buscarPorPlaca(@PathVariable String placa) {
        var veiculo = buscarVeiculoPorPlacaUseCase.buscarPorPlaca(placa);
        return veiculoMapper.toResponse(veiculo);
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable UUID id, @RequestBody @Valid VeiculoRequest request) {
        var veiculoDomain = veiculoMapper.toDomain(request);
        var atualizado = atualizarVeiculoUseCase.atualizar(id, veiculoDomain);
        return veiculoMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarVeiculoUseCase.deletar(id);
    }
}