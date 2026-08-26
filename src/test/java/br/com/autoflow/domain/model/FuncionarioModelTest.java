package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioModelTest {

    @Test
    void builder_e_metodosDeEstadoFuncionamCorretamente() {
        Funcionario f = Funcionario.builder()
                .idFuncionario(UUID.randomUUID())
                .cpf("12345678900")
                .nome("João")
                .telefone("51999999999")
                .email("joao@email.com")
                .genero(Genero.MASCULINO)
                .dataNascimento(LocalDate.of(1990,1,1))
                .cargo(Cargo.MECANICO)
                .build();

        assertEquals("João", f.getNome());
        assertFalse(f.isOcupado());
        assertEquals(0, f.getNr_advertencias());

        f.ocupar();
        assertTrue(f.isOcupado());

        f.liberar();
        assertFalse(f.isOcupado());

        f.adicionarAdvertencia();
        f.adicionarAdvertencia();
        f.adicionarAdvertencia();
        assertTrue(f.deveSerDemitido());
    }
}
