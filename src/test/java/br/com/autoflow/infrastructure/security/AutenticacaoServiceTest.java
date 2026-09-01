package br.com.autoflow.infrastructure.security;

import br.com.autoflow.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacaoServiceTest {

    @Test
    void loadUserByUsername_deveDelegarAoRepositorio() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        UserDetails u = User.withUsername("u").password("p").roles("CLIENTE").build();
        when(repo.findByLogin("u")).thenReturn(u);

        AutenticacaoService svc = new AutenticacaoService(repo);
        UserDetails res = svc.loadUserByUsername("u");

        assertSame(u, res);
        verify(repo).findByLogin("u");
    }
}
