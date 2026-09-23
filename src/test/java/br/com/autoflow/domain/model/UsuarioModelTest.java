package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Perfil;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioModelTest {

    @Test
    void atualizarDadosAcesso_deveModificarLoginEPerfil() {
        Usuario usuario = new Usuario();
        usuario.setLogin("antigo@email.com");
        usuario.setPerfil(Perfil.CLIENTE);

        usuario.atualizarDadosAcesso("novo@email.com", Perfil.ADMIN);

        assertEquals("novo@email.com", usuario.getLogin());
        assertEquals(Perfil.ADMIN, usuario.getPerfil());
    }

    @Test
    void factoryMethods_devemCriarUsuarioCorretamente() {
        PasswordEncoder pe = mock(PasswordEncoder.class);
        when(pe.encode(anyString())).thenReturn("encoded");

        // Usando o construtor vazio + setters conforme a refatoração do Funcionario
        Funcionario f = new Funcionario();
        f.setEmail("f@mail");
        f.setCpf("111");

        Usuario u = Usuario.criarUsuarioParaFuncionario(f, Perfil.MECANICO, "encoded");
        assertEquals("f@mail", u.getLogin());
        assertEquals("encoded", u.getSenha());
        assertEquals(Perfil.MECANICO, u.getPerfil());
        assertEquals(f, u.getFuncionario());

        Cliente c = new Cliente();
        c.setEmail("c@mail");
        c.setDocumento("222");

        Usuario uc = Usuario.criarUsuarioParaCliente(c, Perfil.CLIENTE, "encoded");
        assertEquals("c@mail", uc.getLogin());
        assertEquals("encoded", uc.getSenha());
        assertEquals(Perfil.CLIENTE, uc.getPerfil());
        assertEquals(c, uc.getCliente());
    }
}