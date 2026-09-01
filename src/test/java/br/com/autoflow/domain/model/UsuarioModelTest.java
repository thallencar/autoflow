package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Perfil;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioModelTest {

    @Test
    void getAuthorities_deveRetornarRolesConformePerfil() {
        Usuario admin = Usuario.builder().perfil(Perfil.ADMIN).build();
        Collection<?> authAdmin = admin.getAuthorities();
        assertTrue(authAdmin.stream().anyMatch(a -> a.toString().contains("ROLE_ADMIN")));

        Usuario mec = Usuario.builder().perfil(Perfil.MECANICO).build();
        assertTrue(mec.getAuthorities().stream().anyMatch(a -> a.toString().contains("ROLE_MECANICO")));

        Usuario cli = Usuario.builder().perfil(Perfil.CLIENTE).build();
        assertTrue(cli.getAuthorities().stream().anyMatch(a -> a.toString().contains("ROLE_CLIENTE")));
    }

    @Test
    void factoryMethods_devemCriarUsuarioComSenhaCriptografada() {
        PasswordEncoder pe = mock(PasswordEncoder.class);
        when(pe.encode(anyString())).thenReturn("encoded");

        Funcionario f = Funcionario.builder().email("f@mail").cpf("111").build();
        Usuario u = Usuario.criarUsuarioParaFuncionario(f, Perfil.MECANICO, pe);
        assertEquals("f@mail", u.getUsername());
        assertEquals("encoded", u.getPassword());

        Cliente c = Cliente.builder().email("c@mail").documento("222").build();
        Usuario uc = Usuario.criarUsuarioParaCliente(c, Perfil.CLIENTE, pe);
        assertEquals("c@mail", uc.getUsername());
        assertEquals("encoded", uc.getPassword());
    }
}
