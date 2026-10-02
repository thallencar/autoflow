package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.domain.enums.*;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapperCoverageTest {

    @Autowired
    private EnderecoMapper enderecoMapper;

    @Autowired
    private ClienteMapper clienteMapper;

    @Autowired
    private FuncionarioMapper funcionarioMapper;

    @Autowired
    private VeiculoMapper veiculoMapper;

    @Autowired
    private ServicoMapper servicoMapper;

    @Autowired
    private OrcamentoItemMapper orcamentoItemMapper;

    @Autowired
    private OrcamentoServicoMapper orcamentoServicoMapper;

    @Autowired
    private OrcamentoMapper orcamentoMapper;

    @Autowired
    private OrdemServicoMapper ordemServicoMapper;

    @Test
    void deveMapearEndereco() {
        Endereco endereco = new Endereco(
                UUID.randomUUID(),          // id
                "93500-000",                // cep
                "RS",                       // uf
                "Caxias do Sul",            // cidade
                "Centro",                   // bairro
                "Rua A",                    // logradouro
                123,                        // numero
                "Casa"                      // complemento
        );

        EnderecoResponse response = enderecoMapper.toResponse(endereco);
        assertEquals(endereco.getId(), response.idEndereco());
        assertEquals("93500-000", response.cep());
        assertEquals("Rua A", response.logradouro());

        EnderecoRequest request = new EnderecoRequest("93000-000", "SP", "São Paulo", "Vila Nova", "Av. Brasil", 10, "Ap 1");
        Endereco entity = enderecoMapper.toDomain(request);
        assertEquals("93000-000", entity.getCep());
        assertEquals("SP", entity.getUf());
    }

    @Test
    void deveMapearClienteEFuncionario() {
        Endereco endereco = new Endereco(
                UUID.randomUUID(),          // id
                "93500-000",                // cep
                "rs",                       // uf
                "Caxias do Sul",            // cidade
                "Centro",                   // bairro
                "Rua A",                    // logradouro
                123,                        // numero
                "Casa"                      // complemento
        );

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNome("Ana");
        cliente.setDocumento("12345678909");
        cliente.setEmail("ana@email.com");
        cliente.setTelefone("51999998888");
        cliente.setGenero(Genero.FEMININO);
        cliente.setEndereco(endereco);

        ClienteResponse response = clienteMapper.toResponse(cliente);
        assertEquals(cliente.getId(), response.id());
        assertEquals("Ana", response.nome());
        assertEquals("93500-000", response.endereco().cep());

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
        FuncionarioResponse funcionarioResponse = funcionarioMapper.toResponse(funcionario);
        assertEquals("Carlos", funcionario.getNome());
        assertEquals("carlos@email.com", funcionarioResponse.email());
        assertEquals(Cargo.MECANICO, funcionarioResponse.cargo());
    }

    @Test
    void deveMapearVeiculo() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        VeiculoRequest request = new VeiculoRequest("abc1a23", "Fiat", "Argo", 12000, (short) 2022, "Prata", cliente.getId());

        Veiculo veiculo = veiculoMapper.toDomain(request);
        assertEquals("ABC1A23", veiculo.getPlaca());
        assertEquals(cliente.getId(), veiculo.getClienteId());

        veiculoMapper.updateDomainFromDto(request, veiculo);
        VeiculoResponse response = veiculoMapper.toResponse(veiculo);
        assertEquals(cliente.getId(), response.clienteId());
        assertEquals("ABC1A23", response.placa());
    }

    @Test
    void deveAtualizarEnderecoComUpdateEntity() {
        Endereco endereco = new Endereco(
                UUID.randomUUID(),          // id
                "93500-000",                // cep
                "rs",                       // uf
                "Caxias do Sul",            // cidade
                "Centro",                   // bairro
                "Rua A",                    // logradouro
                123,                        // numero
                "Casa"                      // complemento
        );

        EnderecoRequest req = new EnderecoRequest("22222-222", "SP", "São Paulo", "Bairro B", "Rua Nova", 45, "Apt");
        enderecoMapper.updateDomainFromDto(req, endereco);

        assertEquals("22222-222", endereco.getCep());
        assertEquals("Rua Nova", endereco.getLogradouro());
        assertEquals(45, endereco.getNumero());
    }
}