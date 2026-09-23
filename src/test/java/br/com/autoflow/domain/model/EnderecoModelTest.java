package br.com.autoflow.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoModelTest {

    @Test
    void atualizarDados_deveAtualizarCamposCorretamente() {
        // Instanciação da entidade Endereco via construtor cheio
        Endereco endereco = new Endereco(
                UUID.randomUUID(),
                "11111-111",
                "rj", // testando se normaliza para maiúsculo
                "Rio",
                "Centro",
                "Rua Velha",
                1,
                "Antigo"
        );

        endereco.atualizar(
                "22222-222",
                "sp",
                "São Paulo",
                "Bairro B",
                "Rua Nova",
                45,
                "Apt"
        );

        assertEquals("22222-222", endereco.getCep());
        assertEquals("SP", endereco.getUf()); // Validando se aplicou o toUpperCase()
        assertEquals("São Paulo", endereco.getCidade());
        assertEquals("Bairro B", endereco.getBairro());
        assertEquals("Rua Nova", endereco.getLogradouro());
        assertEquals(45, endereco.getNumero());
        assertEquals("Apt", endereco.getComplemento());
    }

    @Test
    void atualizarDados_quandoUfNula_naoDeveLancarExcecao() {
        Endereco endereco = new Endereco(
                UUID.randomUUID(),
                "11111-111",
                "RS",
                "Porto Alegre",
                "Centro",
                "Rua A",
                10,
                null
        );

        endereco.atualizar(
                "11111-111",
                null,
                "Porto Alegre",
                "Centro",
                "Rua A",
                10,
                null
        );

        assertNull(endereco.getUf());
    }
}