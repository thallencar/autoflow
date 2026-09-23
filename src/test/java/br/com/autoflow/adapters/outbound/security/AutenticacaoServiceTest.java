package br.com.autoflow.adapters.outbound.security;

import br.com.autoflow.adapters.outbound.security.AutenticacaoService;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacaoServiceTest {

    @Test
    void loadUserByUsername_deveRetornarUserDetailsQuandoUsuarioExiste() {
        // Arrange
        UsuarioRepositoryPort repo = mock(UsuarioRepositoryPort.class);

        Usuario usuarioDom = new Usuario(
                UUID.randomUUID(),
                "u",
                "senha123",
                Perfil.CLIENTE,
                null,
                null
        );

        when(repo.findByLogin("u")).thenReturn(Optional.of(usuarioDom));

        AutenticacaoService svc = new AutenticacaoService(repo);

        // Act
        UserDetails userDetails = svc.loadUserByUsername("u");

        // Assert
        assertNotNull(userDetails);
        assertEquals("u", userDetails.getUsername());
        assertEquals("senha123", userDetails.getPassword());
        verify(repo).findByLogin("u");
    }

    @Test
    void loadUserByUsername_deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        UsuarioRepositoryPort repo = mock(UsuarioRepositoryPort.class);
        when(repo.findByLogin("inexistente")).thenReturn(Optional.empty());

        AutenticacaoService svc = new AutenticacaoService(repo);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            svc.loadUserByUsername("inexistente");
        });

        verify(repo).findByLogin("inexistente");
    }
}