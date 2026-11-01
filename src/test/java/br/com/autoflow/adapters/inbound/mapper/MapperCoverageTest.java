package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.domain.enums.*;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "api.security.token.secret=minha-chave-secreta-super-segura-aqui",
        "api.security.token.expiration-minutes=15"
})
class MapperCoverageTest {

    @Autowired
    private ClienteMapper clienteMapper;

    @Autowired
    private FuncionarioMapper funcionarioMapper;

    @Autowired
    private VeiculoMapper veiculoMapper;

    @Autowired
    private ServicoMapper servicoMapper;

    @Autowired
    private OrcamentoServicoMapper orcamentoServicoMapper;

    @Autowired
    private OrcamentoMapper orcamentoMapper;

    @Autowired
    private OrdemServicoMapper ordemServicoMapper;

    @Autowired
    private EnderecoMapper enderecoMapper;

    @Test
    void deveMapearClienteEFuncionario() {
        Endereco endereco = new Endereco(
                UUID.randomUUID(),
                "93500-000",
                "RS",
                "Caxias do Sul",
                "Centro",
                "Rua A",
                123,
                "Casa"
        );

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNome("Ana");
        cliente.setDocumento("12345678909");
        cliente.setEmail("ana@email.com");
        cliente.setTelefone("51999998888");
        cliente.setGenero(Genero.FEMININO);
        cliente.setEndereco(endereco);

        ClienteResponse clienteResponse = clienteMapper.toResponse(cliente);
        assertEquals(cliente.getId(), clienteResponse.id());
        assertEquals("Ana", clienteResponse.nome());
        assertEquals("93500-000", clienteResponse.endereco().cep());

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

        FuncionarioResponse funcionarioResponse = funcionarioMapper.toResponse(funcionario);
        assertEquals("carlos@email.com", funcionarioResponse.email());
        assertEquals(Cargo.MECANICO, funcionarioResponse.cargo());
    }

    @Test
    void deveMapearVeiculo() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        VeiculoRequest request = new VeiculoRequest(
                "abc1a23", "Fiat", "Argo", 12000, (short) 2022, "Prata", cliente.getId()
        );

        Veiculo veiculo = veiculoMapper.toDomain(request);
        assertEquals("ABC1A23", veiculo.getPlaca());
        assertEquals(cliente.getId(), veiculo.getClienteId());

        veiculoMapper.updateEntityFromDto(request, veiculo);
        VeiculoResponse response = veiculoMapper.toResponse(veiculo);
        assertEquals(cliente.getId(), response.clienteId());
        assertEquals("ABC1A23", response.placa());
    }

    @Test
    void deveMapearServicoEOrcamentoServico() {
        ServicoRequest request = new ServicoRequest("Diagnóstico", new BigDecimal("150.00"), 60);
        Servico servico = servicoMapper.toDomain(request);
        assertEquals("Diagnóstico", servico.getDsServico());

        ServicoResponse response = servicoMapper.toResponse(servico);
        assertEquals(new BigDecimal("150.00"), response.vlServico());

        OrcamentoItemRequest itemRequest = new OrcamentoItemRequest(
                1, new BigDecimal("10.00"), new BigDecimal("10.00"), UUID.randomUUID()
        );
        OrcamentoServicoRequest osRequest = new OrcamentoServicoRequest(
                servico.getIdServico(), new BigDecimal("90.00"), List.of(itemRequest)
        );
        OrcamentoServico orcamentoServico = orcamentoServicoMapper.toDomain(osRequest);
        assertEquals(servico.getIdServico(), orcamentoServico.getServico().getIdServico());

        OrcamentoServicoResponse osResponse = orcamentoServicoMapper.toResponse(orcamentoServico);
        assertEquals(new BigDecimal("90.00"), osResponse.maoDeObra());
    }

    @Test
    void deveMapearOrcamentoEOrdemServico() {
        OrcamentoRequest request = new OrcamentoRequest(
                UUID.randomUUID(),
                TipoOrcamento.INICIAL,
                LocalDateTime.now().plusDays(2),
                List.of(new OrcamentoServicoRequest(UUID.randomUUID(), new BigDecimal("80.00"), List.of())),
                List.of()
        );

        Orcamento orcamento = orcamentoMapper.toDomain(request);
        assertEquals(TipoOrcamento.INICIAL, orcamento.getTipoOrcamento());
        assertEquals(StatusOrcamento.PENDENTE, orcamento.getStatus());

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                StatusReservaEstoque.RESERVADO,
                2,
                new BigDecimal("50.00"),
                UUID.randomUUID()
        );

        OrcamentoItemResponse itemResponse = orcamentoMapper.toResponse(item);
        assertEquals(new BigDecimal("100.00"), itemResponse.valorTotal());

        OrdemServico ordem = new OrdemServico();
        ordem.setIdOs(UUID.randomUUID());
        ordem.setStatusOS(StatusOS.EM_DIAGNOSTICO);
        ordem.setDsRelatoCliente("Excesso de barulho");
        ordem.setNrKmEntrada(15000);
        ordem.setServicosExecucao(List.of());

        OrdemServicoResponse response = ordemServicoMapper.toResponse(ordem);
        assertEquals(ordem.getIdOs(), response.idOs());
        assertEquals(StatusOS.EM_DIAGNOSTICO, response.statusOS());

        MetricaOsResponse metrica = ordemServicoMapper.toMetricaResponse(ordem);
        assertEquals(ordem.getIdOs(), metrica.idOs());

        HistoricoVeiculoResponse historico = ordemServicoMapper.toHistoricoResponse(ordem);
        assertEquals(ordem.getIdOs(), historico.idOs());
        assertEquals(StatusOS.EM_DIAGNOSTICO, historico.statusOS());
    }

    @Test
    void deveAtualizarEnderecoComUpdateEntity() {
        Endereco endereco = new Endereco(
                UUID.randomUUID(),
                "93500-000",
                "RS",
                "Caxias do Sul",
                "Centro",
                "Rua A",
                123,
                "Casa"
        );

        EnderecoRequest request = new EnderecoRequest(
                "22222-222", "SP", "São Paulo", "Bairro B", "Rua Nova", 45, "Apt"
        );
        enderecoMapper.updateDomainFromDto(request, endereco);

        assertEquals("22222-222", endereco.getCep());
        assertEquals("Rua Nova", endereco.getLogradouro());
        assertEquals(45, endereco.getNumero());
    }
}
