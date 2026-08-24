package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.domain.enums.*;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapperFullCoverageTest {

    @Test
    void orcamentoMapperVincularFilhos() {
        OrcamentoMapper mapper = Mappers.getMapper(OrcamentoMapper.class);

        OrcamentoItemRequest itemReq = new OrcamentoItemRequest(2, new BigDecimal("5.00"), new BigDecimal("10.00"), UUID.randomUUID());
        OrcamentoServicoRequest servReq = new OrcamentoServicoRequest(UUID.randomUUID(), new BigDecimal("20.00"), List.of(itemReq));
        OrcamentoRequest req = new OrcamentoRequest(UUID.randomUUID(), TipoOrcamento.INICIAL, LocalDateTime.now().plusDays(1), List.of(servReq), List.of(itemReq));

        Orcamento orc = mapper.toEntity(req);
        mapper.vincularFilhos(orc);

        assertNotNull(orc.getServicos());
        assertEquals(1, orc.getServicos().size());
        OrcamentoServico servico = orc.getServicos().get(0);
        assertSame(orc, servico.getOrcamento());
        assertNotNull(servico.getItens());
        assertFalse(servico.getItens().isEmpty());
        assertSame(servico, servico.getItens().get(0).getOrcamentoServico());
    }

    @Test
    void ordemServicoHistoricoIncluiPecas() {
        OrdemServicoMapper mapper = Mappers.getMapper(OrdemServicoMapper.class);

        Servico serv = Servico.builder().idServico(UUID.randomUUID()).dsServico("X").vlServico(new BigDecimal("30.00")).qtTempoEstimadoMin(10).build();
        OsServico osServ = OsServico.builder().servico(serv).build();

        OrcamentoItem item = OrcamentoItem.builder().idEstoque(UUID.randomUUID()).quantidade(1).valorUnitario(new BigDecimal("2.00")).build();
        item.setOrcamentoServico(OrcamentoServico.builder().servico(serv).build());

        OrcamentoServico orcServ = OrcamentoServico.builder().servico(serv).itens(List.of(item)).build();
        item.setOrcamentoServico(orcServ);
        Orcamento orc = Orcamento.builder().servicos(List.of(orcServ)).build();
        orcServ.setOrcamento(orc);
        orc.setItens(List.of(item));

        OrdemServico os = OrdemServico.builder().idOs(UUID.randomUUID()).servicosExecucao(List.of(osServ)).idsOrcamento(List.of(orc)).build();

        HistoricoVeiculoResponse hist = mapper.toHistoricoResponse(os);
        assertNotNull(hist);
        assertEquals(os.getIdOs(), hist.idOs());
        assertFalse(hist.servicosExecucao().isEmpty());
        assertFalse(hist.servicosExecucao().get(0).pecasUtilizadas().isEmpty());
    }

    @Test
    void servicoMapperOperations() {
        ServicoMapper mapper = Mappers.getMapper(ServicoMapper.class);
        ServicoRequest req = new ServicoRequest("Troca de óleo", new BigDecimal("120.00"), 30);
        Servico ent = mapper.toEntity(req);
        assertEquals("Troca de óleo", ent.getDsServico());

        ServicoResponse resp = mapper.toResponse(ent);
        assertEquals(new BigDecimal("120.00"), resp.vlServico());

        mapper.updateEntityFromDto(new ServicoRequest("Troca filtro", new BigDecimal("80.00"), 20), ent);
        assertEquals("Troca filtro", ent.getDsServico());
    }

    @Test
    void orcamentoServicoMapperVincularItens() {
        OrcamentoServicoMapper mapper = Mappers.getMapper(OrcamentoServicoMapper.class);
        OrcamentoItemRequest itemReq = new OrcamentoItemRequest(1, new BigDecimal("3.00"), new BigDecimal("3.00"), UUID.randomUUID());
        OrcamentoServicoRequest req = new OrcamentoServicoRequest(UUID.randomUUID(), new BigDecimal("10.00"), List.of(itemReq));

        OrcamentoServico ent = mapper.toEntity(req);
        OrcamentoItem item = OrcamentoItem.builder().quantidade(1).valorUnitario(new BigDecimal("3.00")).build();
        ent.setItens(List.of(item));
        mapper.vincularItens(ent);
        assertNotNull(ent);
        assertNotNull(ent.getItens());
        assertFalse(ent.getItens().isEmpty());
        assertSame(ent, ent.getItens().get(0).getOrcamentoServico());

        OrcamentoServicoResponse resp = mapper.toResponse(ent);
        assertEquals(ent.getMaoDeObra(), resp.maoDeObra());
    }

    @Test
    void estoqueMapperRoundtrip() {
        EstoqueMapper mapper = Mappers.getMapper(EstoqueMapper.class);
        EstoqueRequest req = new EstoqueRequest("Filtro","Bosch", new BigDecimal("25.00"), 5, 2, TipoItemEstoque.INSUMO);
        Estoque ent = mapper.toEntity(req);
        assertEquals("Filtro", ent.getNomeItem());

        EstoqueResponse resp = mapper.toResponse(ent);
        assertEquals(ent.getNomeItem(), resp.nomeItem());
    }

    @Test
    void funcionarioMapperToEntityAndUpdate() {
        EnderecoMapper enderecoMapper = new EnderecoMapperImpl();
        FuncionarioMapperImpl mapper = new FuncionarioMapperImpl();
        ReflectionTestUtils.setField(mapper, "enderecoMapper", enderecoMapper);

        FuncionarioRequest req = new FuncionarioRequest("11122233344","Joao","51900000000","joao@x.com", Genero.MASCULINO, java.time.LocalDate.of(1990,1,1), Cargo.MECANICO, new EnderecoRequest("90000-000","RS","POA","Centro","Rua A",1,null));
        Funcionario ent = mapper.toEntity(req);
        assertEquals("Joao", ent.getNome());
        assertNotNull(ent.getEndereco());

        Funcionario update = Funcionario.builder().build();
        mapper.updateEntityFromDto(req, update);
        assertEquals("Joao", update.getNome());
    }

    @Test
    void veiculoMapperUpdateAndToResponse() {
        EnderecoMapper enderecoMapper = Mappers.getMapper(EnderecoMapper.class);
        ClienteMapper clienteMapper = new ClienteMapper(enderecoMapper);
        VeiculoMapper mapper = new VeiculoMapper(clienteMapper);

        Cliente cliente = new Cliente(); cliente.setId(UUID.randomUUID());
        VeiculoRequest req = new VeiculoRequest("abc1a23","VW","Golf",1000,(short)2019,"Preto", cliente.getId());
        Veiculo ent = mapper.toEntity(req, cliente);
        assertEquals("ABC1A23", ent.getPlaca());

        VeiculoResponse resp = mapper.toResponse(ent);
        assertEquals(cliente.getId(), resp.clienteId());
    }
}
