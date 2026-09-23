package br.com.autoflow.adapters.outbound.security;

import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityFilter  extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepositoryPort usuarioRepository;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            String login = tokenService.validarToken(token);

            if (login != null && !login.isEmpty()) {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(login);

                if (usuarioOpt.isPresent()) {
                    // Cria o adapter do Spring Security
                    UserDetailsImpl userDetails = new UserDetailsImpl(usuarioOpt.get());

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ","");
    }
}
