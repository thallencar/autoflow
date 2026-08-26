package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.FuncionarioRequest;
import br.com.autoflow.application.dto.FuncionarioResponse;
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

        Funcionario existing = Funcionario.builder()
                .idFuncionario(UUID.randomUUID())
                .cpf("oldcpf")
                .ocupado(true)
                .nr_advertencias(5)
                .build();

        mapper.updateEntityFromDto(req, existing);

        // campos ignorados não devem ser alterados
        assertNotNull(existing.getIdFuncionario());
        assertTrue(existing.isOcupado());
        assertEquals(5, existing.getNr_advertencias());

        // campos atualizáveis devem mudar
        assertEquals("Carlos", existing.getNome());
        assertEquals("22728697039", existing.getCpf());
    }

    @Test
    void toResponse_deveMapearIdCorretamente() {
        FuncionarioMapperImpl mapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(mapper, "enderecoMapper", new EnderecoMapperImpl());

        Funcionario f = Funcionario.builder()
                .idFuncionario(UUID.randomUUID())
                .nome("Maria")
                .email("maria@mail.com")
                .cargo(Cargo.GERENTE)
                .build();

        FuncionarioResponse resp = mapper.toResponse(f);
        assertEquals(f.getIdFuncionario(), resp.id());
        assertEquals("maria@mail.com", resp.email());
    }

    @Test
    void updateEntityFromDto_comEndereco_deveAtualizarEndereco() {
        FuncionarioMapperImpl mapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(mapper, "enderecoMapper", new EnderecoMapperImpl());

        var enderecoReq = new br.com.autoflow.application.dto.EnderecoRequest("93000-000", "SP", "S","B","R",10, "C");
        FuncionarioRequest req = new FuncionarioRequest(
                "22728697039",
                "Carlos",
                "51988887777",
                "carlos@email.com",
                Genero.MASCULINO,
                java.time.LocalDate.of(1988,5,12),
                Cargo.MECANICO,
                enderecoReq
        );

        Funcionario existing = Funcionario.builder()
                .idFuncionario(UUID.randomUUID())
                .cpf("oldcpf")
                .ocupado(false)
                .nr_advertencias(1)
                .endereco(Endereco.builder().cep("00000-000").build())
                .build();

        mapper.updateEntityFromDto(req, existing);

        assertEquals("Carlos", existing.getNome());
        assertEquals("93000-000", existing.getEndereco().getCep());
    }
}

