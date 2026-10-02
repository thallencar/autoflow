package br.com.autoflow.adapters.inbound.mapper;

import br.com.autoflow.adapters.inbound.controller.dto.HistoricoVeiculoResponse;
import br.com.autoflow.adapters.inbound.controller.dto.MetricaOsResponse;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
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
        OrdemServico os = new OrdemServico();
        os.setIdOs(UUID.randomUUID());

        OrdemServicoMapper mapper = Mappers.getMapper(OrdemServicoMapper.class);

        MetricaOsResponse met = mapper.toMetricaResponse(os);
        assertNotNull(met);
        assertEquals(os.getIdOs(), met.idOs());
    }

    @Test
    void toHistoricoResponse_deveMapearPecasCorretamente() {
        Servico serv = new Servico(
                UUID.randomUUID(),          // idServico
                "Alinhamento e Balanceamento", // dsServico
                new BigDecimal("120.00"),     // vlServico
                45                          // qtTempoEstimadoMin
        );

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(),
                StatusReservaEstoque.RESERVADO,
                2,
                new BigDecimal("5.00"),
                UUID.randomUUID()
        );

        OrcamentoServico osServ = new OrcamentoServico(
                UUID.randomUUID(),          // id
                new BigDecimal("90.00"),    // maoDeObra
                serv,                       // servico
                List.of(item)               // itens
        );

        // Associa corretamente usando os métodos da entidade em vez de setters inexistentes
        item.associarOrcamentoServico(osServ);

        Orcamento orc = new Orcamento(
                UUID.randomUUID(),                          // id
                TipoOrcamento.INICIAL,                      // tipoOrcamento
                StatusOrcamento.PENDENTE,                   // status
                LocalDateTime.now(),                        // dataCriacao
                LocalDateTime.now().plusDays(7),            // dataExpiracao
                null,                                       // dataDecisao
                new BigDecimal("10.00"),                    // subtotalPecas
                new BigDecimal("90.00"),                    // maoObra
                new BigDecimal("100.00"),                   // total
                null,                                       // ordemServico
                List.of(osServ),                            // servicos
                List.of(item)                               // itens
        );

        osServ.associarOrcamento(orc);

        OsServico osSvc = new OsServico();
        osSvc.setServico(serv);

        OrdemServico ordem = new OrdemServico();
        ordem.setIdOs(UUID.randomUUID());
        ordem.setServicosExecucao(List.of(osSvc));
        ordem.setIdsOrcamento(List.of(orc));

        OrdemServicoMapper mapper = Mappers.getMapper(OrdemServicoMapper.class);

        HistoricoVeiculoResponse hist = mapper.toHistoricoResponse(ordem);
        assertNotNull(hist);
        assertEquals(ordem.getIdOs(), hist.idOs());
        assertFalse(hist.servicosExecucao().isEmpty());
    }
}