package br.com.autoflow.infrastructure.security;

import br.com.autoflow.domain.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration-minutes}")
    private Long expirationMinutes;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            var builder = JWT.create()
                    .withIssuer("autoflow-api")
                    .withSubject(usuario.getLogin())
                    .withClaim("perfil", usuario.getPerfil().name())
                    .withExpiresAt(gerarDataExpiracao());

            // Se o usuário for um Cliente, adiciona o id do cliente no token
            if (usuario.getCliente() != null) {
                builder.withClaim("clienteId", usuario.getCliente().getId().toString());
            }

            // Se o usuário for um Funcionário, adiciona o id do funcionário no token
            if (usuario.getFuncionario() != null) {
                builder.withClaim("funcionarioId", usuario.getFuncionario().getIdFuncionario().toString());
            }

            return builder.sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validarToken(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("autoflow-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "Token inválido.";
        }
    }

    private Instant gerarDataExpiracao() {
        return LocalDateTime.now().plusMinutes(expirationMinutes).toInstant(ZoneOffset.of("-03:00"));
    }
}
