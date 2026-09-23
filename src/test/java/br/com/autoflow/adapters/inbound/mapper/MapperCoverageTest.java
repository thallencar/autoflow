package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.domain.enums.*;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapperCoverageTest {

    @Test
    void deveMapearEndereco() {
        EnderecoMapper mapper = Mappers.getMapper(EnderecoMapper.class);

        Endereco endereco = new Endereco(
                UUID.randomUUID(),          // id
                "93500-000",                // cep
                "rs",                       // uf (será convertido para "RS" automaticamente pelo construtor)
                "Caxias do Sul",            // cidade
                "Centro",                   // bairro
                "Rua A",                    // logradouro
                123,                        // numero
                "Casa"                      // complemento
        );

        EnderecoResponse response = mapper.toResponse(endereco);
        assertEquals(endereco.getId(), response.idEndereco());
        assertEquals("93500-000", response.cep());
        assertEquals("Rua A", response.logradouro());

        EnderecoRequest request = new EnderecoRequest("93000-000", "SP", "São Paulo", "Vila Nova", "Av. Brasil", 10, "Ap 1");
        Endereco entity = mapper.toDomain(request);
        assertEquals("93000-000", entity.getCep());
        assertEquals("SP", entity.getUf());
    }

    @Test
    void deveMapearClienteEFuncionario() {
        ClienteMapperImpl clienteMapper = new ClienteMapperImpl();
        ReflectionTestUtils.setField(clienteMapper, "enderecoMapper", new EnderecoMapperImpl());

        FuncionarioMapperImpl funcionarioMapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(funcionarioMapper, "enderecoMapper", new EnderecoMapperImpl());

        Endereco endereco = new Endereco(
                UUID.randomUUID(),          // id
                "93500-000",                // cep
                "rs",                       // uf (será convertido para "RS" automaticamente pelo construtor)
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
        assertEquals("90010-100", response.endereco().cep());

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
        VeiculoMapper mapper = Mappers.getMapper(VeiculoMapper.class);

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        VeiculoRequest request = new VeiculoRequest("abc1a23", "Fiat", "Argo", 12000, (short) 2022, "Prata", cliente.getId());

        // Passando o clienteId ou mapeando via request/cliente
        Veiculo veiculo = mapper.toDomain(request);
        assertEquals("ABC1A23", veiculo.getPlaca());
        assertEquals(cliente.getId(), veiculo.getClienteId());

        mapper.updateEntityFromDto(request, veiculo);
        VeiculoResponse response = mapper.toResponse(veiculo);
        assertEquals(cliente.getId(), response.clienteId());
        assertEquals("ABC1A23", response.placa());
    }

    @Test
    void deveMapearServicoEOrcamentoServico() {
        ServicoMapper servicoMapper = Mappers.getMapper(ServicoMapper.class);
        OrcamentoServicoMapper orcamentoServicoMapper = Mappers.getMapper(OrcamentoServicoMapper.class);

        ServicoRequest request = new ServicoRequest("Diagnóstico", new BigDecimal("150.00"), 60);
        Servico servico = servicoMapper.toDomain(request);
        assertEquals("Diagnóstico", servico.getDsServico());

        ServicoResponse response = servicoMapper.toResponse(servico);
        assertEquals(new BigDecimal("150.00"), response.vlServico());

        OrcamentoItemRequest itemRequest = new OrcamentoItemRequest(1, new BigDecimal("10.00"), new BigDecimal("10.00"), UUID.randomUUID());
        OrcamentoServicoRequest osRequest = new OrcamentoServicoRequest(servico.getIdServico(), new BigDecimal("90.00"), List.of(itemRequest));
        OrcamentoServico orcamentoServico = orcamentoServicoMapper.toDomain(osRequest);
        assertEquals(servico.getIdServico(), orcamentoServico.getServico().getIdServico());

        OrcamentoServicoResponse osResponse = orcamentoServicoMapper.toResponse(orcamentoServico);
        assertEquals(new BigDecimal("90.00"), osResponse.maoDeObra());
    }

    @Test
    void deveMapearOrcamentoEOrdemServico() {
        OrcamentoMapper orcamentoMapper = Mappers.getMapper(OrcamentoMapper.class);
        OrdemServicoMapper ordemServicoMapper = Mappers.getMapper(OrdemServicoMapper.class);

        OrcamentoRequest request = new OrcamentoRequest(
                UUID.randomUUID(),
                TipoOrcamento.INICIAL,
                LocalDateTime.now().plusDays(2),
                List.of(new OrcamentoServicoRequest(UUID.randomUUID(), new BigDecimal("80.00"), List.of())),
                List.of()
        );

        Orcamento entity = orcamentoMapper.toEntity(request);
        assertEquals(TipoOrcamento.INICIAL, entity.getTipoOrcamento());
        assertEquals(StatusOrcamento.PENDENTE, entity.getStatus());

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),                          // id
                StatusReservaEstoque.RESERVADO,             // statusReserva
                2,                                          // quantidade
                new BigDecimal("50.00"),                    // valorUnitario
                new BigDecimal("100.00"),                   // valorTotal (será recalculado pelo construtor, se houver lógica para isso)
                UUID.randomUUID(),                          // idEstoque
                null,                                       // orcamentoServico (ou uma instância válida se necessário)
                null                                        // orcamento (ou uma instância válida se necessário)
        );

        OrcamentoItemResponse itemResponse = orcamentoMapper.toResponse(item);
        assertEquals(new BigDecimal("20.00"), itemResponse.valorTotal());

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
        EnderecoMapper mapper = Mappers.getMapper(EnderecoMapper.class);

        Endereco endereco = new Endereco(
                UUID.randomUUID(),          // id
                "93500-000",                // cep
                "rs",                       // uf (será convertido para "RS" automaticamente pelo construtor)
                "Caxias do Sul",            // cidade
                "Centro",                   // bairro
                "Rua A",                    // logradouro
                123,                        // numero
                "Casa"                      // complemento
        );

        EnderecoRequest req = new EnderecoRequest("22222-222","SP","São Paulo","Bairro B","Rua Nova",45,"Apt");
        mapper.updateDomainFromDto(req, endereco);

        assertEquals("22222-222", endereco.getCep());
        assertEquals("Rua Nova", endereco.getLogradouro());
        assertEquals(45, endereco.getNumero());
    }
}