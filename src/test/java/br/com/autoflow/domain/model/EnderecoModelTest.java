package br.com.autoflow.domain.model;

import br.com.autoflow.application.dto.EnderecoRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoModelTest {

    @Test
    void atualizarDados_deveAtualizarCamposQuandoRequestNaoNulo() {
        Endereco endereco = Endereco.builder()
                .id(UUID.randomUUID())
                .cep("11111-111")
                .uf("RJ")
                .cidade("Rio")
                .bairro("Centro")
                .logradouro("Rua Velha")
                .numero(1)
                .complemento("Antigo")
                .build();

        EnderecoRequest req = new EnderecoRequest("22222-222","SP","São Paulo","Bairro B","Rua Nova",45,"Apt");
        endereco.atualizarDados(req);

        assertEquals("22222-222", endereco.getCep());
        assertEquals("SP", endereco.getUf());
        assertEquals("São Paulo", endereco.getCidade());
        assertEquals("Rua Nova", endereco.getLogradouro());
        assertEquals(45, endereco.getNumero());
        assertEquals("Apt", endereco.getComplemento());
    }

    @Test
    void atualizarDados_naoDeveLancarQuandoRequestNulo() {
        Endereco endereco = new Endereco();
        endereco.atualizarDados(null);
        // apenas garantir que não lança e que atributos permanecem nulos
        assertNull(endereco.getCep());
        assertNull(endereco.getUf());
    }
}
