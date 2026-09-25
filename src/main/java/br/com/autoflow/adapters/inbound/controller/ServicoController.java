package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ServicoResponse;
import br.com.autoflow.adapters.inbound.mapper.ServicoMapper;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.inbound.servico.*;
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

    private final CriarServicoUseCase criarServicoUseCase;
    private final ListarServicosUseCase listarServicosUseCase;
    private final BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;
    private final AtualizarServicoUseCase atualizarServicoUseCase;
    private final DeletarServicoUseCase deletarServicoUseCase;

    private final ServicoMapper servicoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse criar(@RequestBody @Valid ServicoRequest request) {
        Servico domain = servicoMapper.toDomain(request);
        Servico salvo = criarServicoUseCase.criar(domain);
        return servicoMapper.toResponse(salvo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<ServicoResponse> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "idServico", direction = Sort.Direction.ASC) Pageable pageable) {
        return listarServicosUseCase.listarTodos(pageable)
                .map(servicoMapper::toResponse);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ServicoResponse buscarPorId(@PathVariable UUID id) {
        Servico servico = buscarServicoPorIdUseCase.buscarPorId(id);
        return servicoMapper.toResponse(servico);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ServicoResponse atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid ServicoRequest request) {
        Servico domain = servicoMapper.toDomain(request);
        Servico atualizado = atualizarServicoUseCase.atualizar(id, domain);
        return servicoMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        deletarServicoUseCase.deletar(id);
    }
}