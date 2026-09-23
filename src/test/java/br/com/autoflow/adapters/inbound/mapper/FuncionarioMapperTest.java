package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.EnderecoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioRequest;
import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioResponse;

import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Funcionario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioMapperTest {

    @Test
    void updateEntityFromDto_deveManterCamposIgnored() {
        FuncionarioMapperImpl mapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(mapper, "enderecoMapper", new EnderecoMapperImpl());

        FuncionarioRequest req = new FuncionarioRequest(
                "22728697039",
                "Carlos",
                "51988887777",
                "carlos@email.com",
                Genero.MASCULINO,
                LocalDate.of(1988, 5, 12),
                Cargo.MECANICO,
                null
        );

        UUID originalId = UUID.randomUUID();
        Funcionario existing = new Funcionario(
                originalId,
                "oldcpf",
                "Carlos da Silva",
                "51900000000",
                "old@email.com",
                Genero.FEMININO,
                LocalDate.of(1990, 1, 1),
                Cargo.RECEPCIONISTA,
                null,
                true, // ocupado
                5     // nrAdvertencias
        );

        mapper.updateEntityFromDto(req, existing);

        // Verifica que o ID, ocupado e nrAdvertencias foram preservados (ignorados pelo mapper)
        assertEquals(originalId, existing.getId());
        assertTrue(existing.isOcupado());
        assertEquals(5, existing.getNrAdvertencias());

        // Verifica que os campos permitidos foram atualizados pelo DTO
        assertEquals("Carlos", existing.getNome());
        assertEquals("22728697039", existing.getCpf());
    }

    @Test
    void toResponse_deveMapearIdCorretamente() {
        FuncionarioMapperImpl mapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(mapper, "enderecoMapper", new EnderecoMapperImpl());

        UUID id = UUID.randomUUID();
        Funcionario f = new Funcionario(
                id, "11122233344", "Maria", "51999998888", "maria@mail.com",
                Genero.FEMININO, LocalDate.of(1995, 10, 10), Cargo.GERENTE, null, false, 0
        );

        FuncionarioResponse resp = mapper.toResponse(f);
        assertEquals(id, resp.id());
        assertEquals("maria@mail.com", resp.email());
        assertEquals("Maria", resp.nome());
    }

    @Test
    void updateEntityFromDto_comEndereco_deveAtualizarEndereco() {
        FuncionarioMapperImpl mapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(mapper, "enderecoMapper", new EnderecoMapperImpl());

        var enderecoReq = new EnderecoRequest("93000-000", "RS", "Ivoti", "Centro", "Rua Principal", 100, "Casa");
        FuncionarioRequest req = new FuncionarioRequest(
                "22728697039",
                "Carlos",
                "51988887777",
                "carlos@email.com",
                Genero.MASCULINO,
                LocalDate.of(1988,5,12),
                Cargo.MECANICO,
                enderecoReq
        );

        Endereco enderecoAntigo = new Endereco();
        enderecoAntigo.setCep("00000-000");

        Funcionario existing = new Funcionario(
                UUID.randomUUID(), "22728697039", "Carlos Antigo", "51911111111", "antigo@mail.com",
                Genero.MASCULINO, LocalDate.of(1988,5,12), Cargo.MECANICO, enderecoAntigo, false, 1
        );

        mapper.updateEntityFromDto(req, existing);

        assertEquals("Carlos", existing.getNome());
        assertEquals("93000-000", existing.getEndereco().getCep());
        assertEquals("Ivoti", existing.getEndereco().getCidade());
    }
}