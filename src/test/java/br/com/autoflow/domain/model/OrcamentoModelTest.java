package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrcamentoModelTest {

    @Test
    void aprovar_e_recusar_deveAtualizarStatusEItens() {
        OrcamentoItem item = OrcamentoItem.builder()
                .idEstoque(UUID.randomUUID())
                .quantidade(1)
                .valorUnitario(new BigDecimal("10.00"))
                .valorTotal(new BigDecimal("10.00"))
                .build();

        Servico servico = Servico.builder().idServico(UUID.randomUUID()).dsServico("Serv").vlServico(new BigDecimal("50.00")).build();
        OrcamentoServico os = OrcamentoServico.builder().maoDeObra(new BigDecimal("20.00")).servico(servico).itens(List.of(item)).build();
        item.setOrcamentoServico(os);

        Orcamento orc = Orcamento.builder()
                .id(UUID.randomUUID())
                .tipoOrcamento(TipoOrcamento.INICIAL)
                .dataCriacao(LocalDateTime.now())
                .dataExpiracao(LocalDateTime.now().plusDays(1))
                .servicos(List.of(os))
                .build();
        os.setOrcamento(orc);

        orc.aprovar();
        assertEquals(StatusOrcamento.APROVADO, orc.getStatus());
        assertEquals(StatusReservaEstoque.VENDIDO, item.getStatusReserva());

        // reset for recusar
        item.setStatusReserva(StatusReservaEstoque.RESERVADO);
        orc = Orcamento.builder().tipoOrcamento(TipoOrcamento.INICIAL).dataCriacao(LocalDateTime.now()).dataExpiracao(LocalDateTime.now().plusDays(1)).servicos(List.of(os)).build();
        os.setOrcamento(orc);

        orc.recusar();
        assertEquals(StatusOrcamento.RECUSADO, orc.getStatus());
        assertEquals(StatusReservaEstoque.CANCELADO, item.getStatusReserva());
    }

    @Test
    void aplicarNovoStatus_invalido_deveLancar() {
        Orcamento orc = Orcamento.builder().tipoOrcamento(TipoOrcamento.INICIAL).dataCriacao(LocalDateTime.now()).dataExpiracao(LocalDateTime.now().plusDays(1)).build();
        assertThrows(RuntimeException.class, () -> orc.aplicarNovoStatus(StatusOrcamento.PENDENTE));
    }

    @Test
    void recalcularTotais_deveSomarMaoDeObraEItens() {
        OrcamentoItem item = OrcamentoItem.builder().quantidade(2).valorUnitario(new BigDecimal("10.00")).build();
        Servico servico = Servico.builder().idServico(UUID.randomUUID()).dsServico("Serv").vlServico(new BigDecimal("50.00")).build();
        OrcamentoServico os = OrcamentoServico.builder().maoDeObra(new BigDecimal("20.00")).servico(servico).itens(List.of(item)).build();
        item.setOrcamentoServico(os);

        Orcamento orc = Orcamento.builder().servicos(List.of(os)).build();
        os.setOrcamento(orc);

        orc.recalcularTotais();
        assertEquals(new BigDecimal("20.00"), orc.getMaoObra());
        assertEquals(new BigDecimal("20.00"), orc.getSubtotalPecas());
        assertEquals(new BigDecimal("40.00"), orc.getTotal());
    }
}
