package br.com.autoflow.infrastructure.security;

import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private final String secret = "minha-chave-secreta-de-teste-123456";
    private final Long expirationMinutes = 60L;

    @BeforeEach
    void setUp() {
        // Injeta os valores das anotações @Value diretamente no serviço para o teste unitário
        ReflectionTestUtils.setField(tokenService, "secret", secret);
        ReflectionTestUtils.setField(tokenService, "expirationMinutes", expirationMinutes);
    }

    private Usuario criarUsuarioBase(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setLogin("usuario@autoflow.com");
        usuario.setPerfil(perfil);
        return usuario;
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido com claim de Funcionario quando o usuário for um funcionário")
    void deveGerarTokenParaFuncionario() {
        // Arrange
        UUID funcionarioId = UUID.randomUUID();
        Funcionario funcionario = new Funcionario();
        funcionario.setIdFuncionario(funcionarioId);

        Usuario usuario = criarUsuarioBase(Perfil.ADMIN);
        usuario.setFuncionario(funcionario);

        // Act
        String token = tokenService.gerarToken(usuario);

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());

        DecodedJWT jwt = JWT.decode(token);
        assertEquals("autoflow-api", jwt.getIssuer());
        assertEquals("usuario@autoflow.com", jwt.getSubject());
        assertEquals("ADMIN", jwt.getClaim("perfil").asString());
        assertEquals(funcionarioId.toString(), jwt.getClaim("funcionarioId").asString());
        assertTrue(jwt.getClaim("clienteId").isMissing());
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido com claim de Cliente quando o usuário for um cliente")
    void deveGerarTokenParaCliente() {
        // Arrange
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        Usuario usuario = criarUsuarioBase(Perfil.MECANICO);
        usuario.setCliente(cliente);

        // Act
        String token = tokenService.gerarToken(usuario);

        // Assert
        assertNotNull(token);
        DecodedJWT jwt = JWT.decode(token);
        assertEquals("autoflow-api", jwt.getIssuer());
        assertEquals("usuario@autoflow.com", jwt.getSubject());
        assertEquals("MECANICO", jwt.getClaim("perfil").asString());
        assertEquals(clienteId.toString(), jwt.getClaim("clienteId").asString());
        assertTrue(jwt.getClaim("funcionarioId").isMissing());
    }
        @Test
        @DisplayName("Deve retornar mensagem de erro para token malformatado ou assinado com outra chave secret")
        void deveRetornarMensagemErroParaTokenInvalido() {
            // Arrange
            String tokenInvalido = "header.payloadComAssinaturaInvalida.signature";

            // Act
            String resultado = tokenService.validarToken(tokenInvalido);

            // Assert
            assertEquals("", resultado);
        }

        @Test
        @DisplayName("Deve retornar mensagem de erro quando o token estiver expirado")
        void deveRetornarMensagemErroParaTokenExpirado() {
            // Arrange: Força o tempo de expiração para um valor negativo (-10 min)
            ReflectionTestUtils.setField(tokenService, "expirationMinutes", -10L);
            Usuario usuario = criarUsuarioBase(Perfil.ADMIN);
            String tokenExpirado = tokenService.gerarToken(usuario);

            // Act
            String resultado = tokenService.validarToken(tokenExpirado);

            // Assert
            assertEquals("", resultado);
        }
    }