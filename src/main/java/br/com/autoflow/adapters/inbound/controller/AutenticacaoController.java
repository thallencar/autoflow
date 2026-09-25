package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.inbound.controller.dto.LoginRequest;
import br.com.autoflow.adapters.inbound.controller.dto.TokenResponse;
import br.com.autoflow.adapters.outbound.security.TokenService;
import br.com.autoflow.adapters.outbound.security.UserDetailsImpl;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.login(), request.senha());
        var auth = authenticationManager.authenticate(usernamePassword);

        if (auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            Usuario usuario = userDetails.getUsuario();

            if (usuario == null) {
                throw new EntidadeNaoEncontradaException("Usuário não encontrado no contexto de autenticação.", null);
            }

            String token = tokenService.gerarToken(usuario);
            return new TokenResponse(token);
        }

        throw new EntidadeNaoEncontradaException("Não foi possível autenticar o usuário.", null);
    }
}