package br.com.autoflow.adapters.inbound.controller;

import br.com.autoflow.adapters.outbound.security.TokenService;
import br.com.autoflow.adapters.outbound.security.UserDetailsImpl;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    // Adicione o mock do repositório para que o AutenticacaoService encontre o usuário no teste unitário standalone
    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @InjectMocks
    private AutenticacaoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(new Validator() {
                    @Override
                    public boolean supports(Class<?> clazz) {
                        return true;
                    }

                    @Override
                    public void validate(Object target, Errors errors) {
                        // Desativa a validação do DTO no teste unitário do Controller
                    }
                })
                .build();
    }

    private String criarJsonLogin(String login, String senha) {
        return """
                {
                  "login": "%s",
                  "senha": "%s"
                }
                """.formatted(login, senha);
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK e o Token JWT quando as credenciais forem válidas")
        void deveAutenticarComSucesso() throws Exception {
            // Arrange
            String login = "carlos@gmail.com";
            String senha = "123";
            String tokenEsperado = "header.payload.signature_jwt_fake";
            UUID idUsuario = UUID.randomUUID();

            // 1. Instância do usuário com ID preenchido
            Usuario usuario = new Usuario();
            usuario.setId(idUsuario);
            usuario.setLogin(login);

            UserDetailsImpl userDetailsMock = mock(UserDetailsImpl.class);
            when(userDetailsMock.getUsuario()).thenReturn(usuario);

            Authentication authenticationMock = mock(Authentication.class);
            when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authenticationMock);

            // 4. Mock da geração do Token
            when(tokenService.gerarToken(any(Usuario.class))).thenReturn(tokenEsperado);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonLogin(login, senha)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(tokenEsperado));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).gerarToken(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve propagar exceção de BadCredentialsException quando login ou senha forem inválidos")
        void deveLancarExcecaoQuandoCredenciaisInvalidas() {
            // Arrange
            String login = "carlos@gmail.com";
            String senha ="senha_errada";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Usuário ou senha inválidos"));

            // Act
            var exception = assertThrows(Exception.class, () ->
                    mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(criarJsonLogin(login, senha)))
            );

            // Assert
            assertTrue(exception.getCause() instanceof BadCredentialsException);
            assertEquals("Usuário ou senha inválidos", exception.getCause().getMessage());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }
}