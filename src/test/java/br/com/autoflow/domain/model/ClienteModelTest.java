package br.com.autoflow.domain.model;

import br.com.autoflow.application.dto.ClienteUpdateRequest;
import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.domain.enums.Genero;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClienteModelTest {

    @Test
    void atualizarDados_deveAtualizarEnderecoQuandoPresente() {
        Endereco end = Endereco.builder().id(UUID.randomUUID()).cep("11111-111").uf("RS").cidade("C").bairro("B").logradouro("L").numero(1).build();
        Cliente c = Cliente.builder().id(UUID.randomUUID()).nome("A").documento("123").email("a@mail").dataNascimento(LocalDate.now()).telefone("123").genero(Genero.MASCULINO).endereco(end).build();

        ClienteUpdateRequest req = new ClienteUpdateRequest("B","b@mail","999",Genero.FEMININO,new EnderecoRequest("22222-222","SP","S","NB","Rua",10,""));
        c.atualizarDados(req);

        assertEquals("B", c.getNome());
        assertEquals("b@mail", c.getEmail());
        assertEquals("22222-222", c.getEndereco().getCep());
    }
}
