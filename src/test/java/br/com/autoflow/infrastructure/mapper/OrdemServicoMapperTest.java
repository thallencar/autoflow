package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.HistoricoVeiculoResponse;
import br.com.autoflow.application.dto.MetricaOsResponse;
import br.com.autoflow.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoMapperTest {

    @Test
    void toMetricaResponse_deveMapearValoresMesmoNulos() {
        OrdemServico os = OrdemServico.builder().idOs(UUID.randomUUID()).build();
        OrdemServicoMapper mapper = Mappers.getMapper(OrdemServicoMapper.class);

        MetricaOsResponse met = mapper.toMetricaResponse(os);
        assertNotNull(met);
        assertEquals(os.getIdOs(), met.idOs());
    }

    @Test
    void toHistoricoResponse_deveMapearPecasCorretamente() {
        Servico serv = Servico.builder().idServico(UUID.randomUUID()).dsServico("S").vlServico(new BigDecimal("10.00")).build();
        OrcamentoItem item = OrcamentoItem.builder().idEstoque(UUID.randomUUID()).quantidade(2).valorUnitario(new BigDecimal("5.00")).valorTotal(new BigDecimal("10.00")).build();
        OrcamentoServico osServ = OrcamentoServico.builder().servico(serv).itens(List.of(item)).build();
        Orcamento orc = Orcamento.builder().itens(List.of(item)).servicos(List.of(osServ)).build();
        item.setOrcamentoServico(osServ);
        osServ.setOrcamento(orc);

        OsServico osSvc = new OsServico();
        osSvc.setServico(serv);

        OrdemServico ordem = OrdemServico.builder().idOs(UUID.randomUUID()).servicosExecucao(List.of(osSvc)).idsOrcamento(List.of(orc)).build();
        OrdemServicoMapper mapper = Mappers.getMapper(OrdemServicoMapper.class);

        HistoricoVeiculoResponse hist = mapper.toHistoricoResponse(ordem);
        assertNotNull(hist);
        assertEquals(ordem.getIdOs(), hist.idOs());
        assertFalse(hist.servicosExecucao().isEmpty());
    }
}
