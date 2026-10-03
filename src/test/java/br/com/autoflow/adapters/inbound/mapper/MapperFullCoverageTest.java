package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapperFullCoverageTest {

    @Autowired
    private OrcamentoMapper orcamentoMapper;

    @Autowired
    private OrdemServicoMapper ordemServicoMapper;

    @Test
    void orcamentoMapperVincularFilhos() {
        OrcamentoItemRequest itemReq = new OrcamentoItemRequest(2, new BigDecimal("5.00"), new BigDecimal("10.00"), UUID.randomUUID());
        OrcamentoServicoRequest servReq = new OrcamentoServicoRequest(UUID.randomUUID(), new BigDecimal("20.00"), List.of(itemReq));
        OrcamentoRequest req = new OrcamentoRequest(UUID.randomUUID(), TipoOrcamento.INICIAL, LocalDateTime.now().plusDays(1), List.of(servReq), List.of(itemReq));

        Orcamento orc = orcamentoMapper.toDomain(req);
        orcamentoMapper.vincularFilhos(orc);

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
                new ArrayList<>()
        );

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                null,
                1,
                new BigDecimal("2.00"),
                UUID.randomUUID()
        );
        item.associarOrcamentoServico(orcServ);
        orcServ.getItens().add(item);

        Orcamento orc = new Orcamento();
        orc.setId(UUID.randomUUID());
        orc.setServicos(List.of(orcServ));
        orcServ.associarOrcamento(orc);

        OrdemServico os = new OrdemServico(
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(orc),
                List.of(osServ)
        );

        HistoricoVeiculoResponse hist = ordemServicoMapper.toHistoricoResponse(os);

        assertNotNull(hist);
        assertEquals(os.getIdOs(), hist.idOs());
    }

    @Test
    void servicoMapperOperations() {
        ServicoMapper servicoMapper = Mappers.getMapper(ServicoMapper.class);
        ServicoRequest req = new ServicoRequest("Troca de óleo", new BigDecimal("120.00"), 30);
        Servico ent = servicoMapper.toDomain(req);
        assertEquals("Troca de óleo", ent.getDsServico());

        ServicoResponse resp = servicoMapper.toResponse(ent);
        assertEquals(new BigDecimal("120.00"), resp.vlServico());

        servicoMapper.updateDomainFromDto(new ServicoRequest("Troca filtro", new BigDecimal("80.00"), 20), ent);
        assertEquals("Troca filtro", ent.getDsServico());
    }

    @Test
    void estoqueMapperRoundtrip() {
        EstoqueMapper estoqueMapper = Mappers.getMapper(EstoqueMapper.class);
        EstoqueRequest req = new EstoqueRequest("Filtro", "Bosch", new BigDecimal("25.00"), 5, 2, TipoItemEstoque.INSUMO);
        Estoque ent = estoqueMapper.toDomain(req);
        assertEquals("Filtro", ent.getNomeItem());

        EstoqueResponse resp = estoqueMapper.toResponse(ent);
        assertEquals(ent.getNomeItem(), resp.nomeItem());
    }

    @Test
    void veiculoMapperToDomainAndToResponse() {
        VeiculoMapper veiculoMapper = Mappers.getMapper(VeiculoMapper.class);

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        VeiculoRequest req = new VeiculoRequest("abc1a23", "VW", "Golf", 1000, (short) 2019, "Preto", cliente.getId());

        Veiculo ent = veiculoMapper.toDomain(req);
        assertEquals("ABC1A23", ent.getPlaca());

        VeiculoResponse resp = veiculoMapper.toResponse(ent);
        assertEquals(cliente.getId(), resp.clienteId());
    }
}