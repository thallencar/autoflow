package br.com.autoflow.infrastructure.security;

import br.com.autoflow.domain.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar o usuário no SecurityContext quando o token Bearer for válido")
    void deveAutenticarQuandoTokenValido() throws ServletException, IOException {
        // Arrange
        String token = "jwt_token_valido";
        String login = "carlos@gmail.com";

        UserDetails userDetailsMock = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validarToken(token)).thenReturn(login);
        when(usuarioRepository.findByLogin(login)).thenReturn(userDetailsMock);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetailsMock, authentication.getPrincipal());

        verify(tokenService).validarToken(token);
        verify(usuarioRepository).findByLogin(login);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o cabeçalho Authorization for nulo")
    void naoDeveAutenticarQuandoHeaderAusente() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenService, usuarioRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o cabeçalho Authorization não começar com 'Bearer '")
    void naoDeveAutenticarQuandoHeaderNaoIniciaComBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic dXN1YXJpbzpzZW5oYQ==");

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenService, usuarioRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o token for inválido (retornar string vazia)")
    void naoDeveAutenticarQuandoTokenInvalido() throws ServletException, IOException {
        // Arrange
        String token = "jwt_token_invalido";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validarToken(token)).thenReturn(""); // Retorna string vazia

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService).validarToken(token);
        verifyNoInteractions(usuarioRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o login for válido mas o usuário não for encontrado no banco")
    void naoDeveAutenticarQuandoUsuarioNaoEncontrado() throws ServletException, IOException {
        // Arrange
        String token = "jwt_token_valido";
        String login = "inexistente@gmail.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validarToken(token)).thenReturn(login);
        when(usuarioRepository.findByLogin(login)).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService).validarToken(token);
        verify(usuarioRepository).findByLogin(login);
        verify(filterChain).doFilter(request, response);
    }
}