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

        Usuario usuario = null;
        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            usuario = userDetails.getUsuario();
        } else if (principal instanceof Usuario u) {
            usuario = u;
        }

        if (usuario == null) {
            throw new EntidadeNaoEncontradaException("Não foi possível autenticar o usuário.", null);
        }

        String token = tokenService.gerarToken(usuario);
        return new TokenResponse(token);
    }
}