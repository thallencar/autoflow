package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.adapters.inbound.mapper.*;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        UUID idServico = UUID.randomUUID();

        Servico serv = new Servico(
                idServico,
                "Troca de Óleo",
                new BigDecimal("30.00"),
                10
        );

        OsServico osServ = new OsServico(
                UUID.randomUUID(),
                null,
                serv,
                null,
                null
        );

        OrcamentoServico orcServ = new OrcamentoServico(
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                serv,
                new ArrayList<>(),
                null
        );

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                null,
                1,
                new BigDecimal("2.00"),
                null,
                UUID.randomUUID(),
                orcServ,
                null
        );

        orcServ.setItens(List.of(item));

        Orcamento orc = new Orcamento(
                UUID.randomUUID(),
                TipoOrcamento.INICIAL,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                (OrdemServico) List.of(item),
                List.of(orcServ),
                null
        );

        orcServ.setOrcamento(orc);

        OrdemServico os = new OrdemServico(
                UUID.randomUUID(),             // idOs
                null,                          // statusOS
                null,                          // dsRelatoCliente
                null,                          // dsDiagnostico
                null,                          // stTermoAceito
                null,                          // dtAceiteTermo
                null,                          // nrKmEntrada
                null,                          // dtAberturaOs
                null,                          // dtInicioDiagnostico
                null,                          // dtFimDiagnostico
                null,                          // dtAprovacaoOrcamento
                null,                          // dataInicioExecucao
                null,                          // dataFimExecucao
                null,                          // dtEncerramentoOs
                null,                          // dtReagendamentoOs
                null,                          // stPagamento
                null,                          // dsMotivoCancelamento
                null,                          // taxaPermanencia
                null,                          // idCliente
                null,                          // idVeiculo
                null,                          // idFuncionario
                List.of(orc),                  // idsOrcamento
                List.of(osServ)                // servicosExecucao
        );

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
        Servico ent = mapper.toDomain(req);
        assertEquals("Troca de óleo", ent.getDsServico());

        ServicoResponse resp = mapper.toResponse(ent);
        assertEquals(new BigDecimal("120.00"), resp.vlServico());

        mapper.updateDomainFromDto(new ServicoRequest("Troca filtro", new BigDecimal("80.00"), 20), ent); // Alterado de updateEntityFromDto para updateDomainFromDto
        assertEquals("Troca filtro", ent.getDsServico());
    }

    @Test
    void orcamentoServicoMapperVincularItens() {
        OrcamentoServicoMapper mapper = Mappers.getMapper(OrcamentoServicoMapper.class);
        OrcamentoItemRequest itemReq = new OrcamentoItemRequest(1, new BigDecimal("3.00"), new BigDecimal("3.00"), UUID.randomUUID());
        OrcamentoServicoRequest req = new OrcamentoServicoRequest(UUID.randomUUID(), new BigDecimal("10.00"), List.of(itemReq));

        OrcamentoServico ent = mapper.toDomain(req); // Alterado de toEntity para toDomain

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                null,
                1,
                new BigDecimal("3.00"),
                null,
                UUID.randomUUID(),
                null,
                null
        );

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
        EstoqueRequest req = new EstoqueRequest("Filtro", "Bosch", new BigDecimal("25.00"), 5, 2, TipoItemEstoque.INSUMO);
        Estoque ent = mapper.toDomain(req); // Alterado de toEntity para toDomain
        assertEquals("Filtro", ent.getNomeItem());

        EstoqueResponse resp = mapper.toResponse(ent);
        assertEquals(ent.getNomeItem(), resp.nomeItem());
    }

    @Test
    void veiculoMapperToDomainAndToResponse() {
        VeiculoMapper mapper = Mappers.getMapper(VeiculoMapper.class);

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        VeiculoRequest req = new VeiculoRequest("abc1a23", "VW", "Golf", 1000, (short) 2019, "Preto", cliente.getId());

        Veiculo ent = mapper.toDomain(req);
        assertEquals("ABC1A23", ent.getPlaca());

        VeiculoResponse resp = mapper.toResponse(ent);
        assertEquals(cliente.getId(), resp.clienteId());
    }
}