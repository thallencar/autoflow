package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.domain.enums.*;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapperCoverageTest {

    @Autowired
    private FuncionarioMapper funcionarioMapper;

    @Test
    void deveMapearClienteEFuncionario() {
        FuncionarioRequest request = new FuncionarioRequest(
                "22728697039",
                "Carlos",
                "51988887777",
                "carlos@email.com",
                Genero.MASCULINO,
                LocalDate.of(1988, 5, 12),
                Cargo.MECANICO,
                new EnderecoRequest("90000-000", "RS", "Porto Alegre", "Centro", "Rua B", 12, "Casa")
        );

        Funcionario funcionario = funcionarioMapper.toDomain(request);
        assertNotNull(funcionario);
        assertEquals("Carlos", funcionario.getNome());
    }
}