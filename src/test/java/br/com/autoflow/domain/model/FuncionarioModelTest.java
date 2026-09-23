package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioModelTest {

    @Test
    void construtor_e_metodosDeEstadoFuncionamCorretamente() {
        Funcionario f = new Funcionario(
                UUID.randomUUID(),
                "12345678900",
                "João",
                "51999999999",
                "joao@email.com",
                Genero.MASCULINO,
                LocalDate.of(1990, 1, 1),
                Cargo.MECANICO,
                null,
                false,
                0
        );

        assertEquals("João", f.getNome());
        assertFalse(f.isOcupado());
        assertEquals(0, f.getNrAdvertencias());

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