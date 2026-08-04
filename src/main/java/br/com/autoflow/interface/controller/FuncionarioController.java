package br.com.autoflow.presentation.controller;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
import br.com.autoflow.application.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/funcionarios")
public class FuncionarioController {
    private final FuncionarioService cadastrarFuncionarioUseCase;

    public FuncionarioController(FuncionarioService cadastrarFuncionarioUseCase) {
        this.cadastrarFuncionarioUseCase = cadastrarFuncionarioUseCase;
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponse> cadastrar(@RequestBody @Valid FuncionarioRequest request) {
        FuncionarioResponse response = cadastrarFuncionarioUseCase.executar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
