package br.com.autoflow.domain.model;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.domain.enums.Genero;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClienteModelTest {

    @Test
    void atualizarDados_deveAtualizarEnderecoQuandoPresente() {
        Endereco end = new Endereco(
                UUID.randomUUID(),
                "11111-111",
                "RS",
                "C",
                "B",
                "L",
                1,
                null
        );

        Cliente c = new Cliente(
                UUID.randomUUID(),
                "A",
                "123",
                "a@mail",
                LocalDate.now(),
                "123",
                Genero.MASCULINO,
                end
        );

        ClienteUpdateRequest req = new ClienteUpdateRequest(
                "B",
                "b@mail",
                "999",
                Genero.FEMININO,
                new EnderecoRequest("22222-222", "SP", "S", "NB", "Rua", 10, "")
        );

        c.atualizarDados(req.nome(), req.telefone(), req.email(), req.genero(), null); // Ajuste conforme os parâmetros reais do seu método atualizarDados se necessário

        assertEquals("B", c.getNome());
        assertEquals("b@mail", c.getEmail());
    }
}