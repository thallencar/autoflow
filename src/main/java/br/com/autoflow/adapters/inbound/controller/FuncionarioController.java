package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioRequest;
import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioResponse;
import br.com.autoflow.adapters.inbound.mapper.FuncionarioMapper;
import br.com.autoflow.application.usecase.FuncionarioUseCase;
import br.com.autoflow.domain.model.Funcionario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioUseCase useCase;
    private final FuncionarioMapper funcionarioMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse criar(@RequestBody @Valid FuncionarioRequest request) {
        Funcionario domain = funcionarioMapper.toDomain(request);
        Funcionario salvo = useCase.criar(domain);
        return funcionarioMapper.toResponse(salvo);
    }

    @GetMapping
    public List<FuncionarioResponse> listar() {
        return useCase.listar().stream()
                .map(funcionarioMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable UUID id) {
        Funcionario funcionario = useCase.buscar(id);
        return funcionarioMapper.toResponse(funcionario);
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(@PathVariable UUID id, @RequestBody @Valid FuncionarioRequest request) {
        Funcionario domain = funcionarioMapper.toDomain(request);
        Funcionario atualizado = useCase.atualizar(id, domain);
        return funcionarioMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        useCase.deletar(id);
    }

    @PatchMapping("/{id}/advertencia")
    @ResponseStatus(HttpStatus.OK)
    public String registrarAdvertencia(@PathVariable UUID id) {
        return useCase.registrarAdvertencia(id);
    }
}