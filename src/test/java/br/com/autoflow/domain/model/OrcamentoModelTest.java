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
        // Instanciação das entidades base
        UUID idEstoque = UUID.randomUUID();
        UUID idServico = UUID.randomUUID();
        UUID idOrcamento = UUID.randomUUID();

        OrcamentoItem item = new OrcamentoItem();
        item.setIdEstoque(idEstoque);
        item.setQuantidade(1);
        item.setValorUnitario(new BigDecimal("10.00"));
        item.setValorTotal(new BigDecimal("10.00"));

        Servico servico = new Servico(idServico, "Serv", new BigDecimal("50.00"), 30);

        OrcamentoServico os = new OrcamentoServico();
        os.setMaoDeObra(new BigDecimal("20.00"));
        os.setServico(servico);
        os.setItens(List.of(item));
        item.setOrcamentoServico(os);

        Orcamento orc = new Orcamento();
        orc.setId(idOrcamento);
        orc.setTipoOrcamento(TipoOrcamento.INICIAL);
        orc.setDataCriacao(LocalDateTime.now());
        orc.setDataExpiracao(LocalDateTime.now().plusDays(1));
        orc.setServicos(List.of(os));
        os.setOrcamento(orc);

        orc.aprovar();
        assertEquals(StatusOrcamento.APROVADO, orc.getStatus());
        assertEquals(StatusReservaEstoque.VENDIDO, item.getStatusReserva());

        // Reset para testar a recusa
        item.setStatusReserva(StatusReservaEstoque.RESERVADO);

        Orcamento orcRecusar = new Orcamento();
        orcRecusar.setId(idOrcamento);
        orcRecusar.setTipoOrcamento(TipoOrcamento.INICIAL);
        orcRecusar.setDataCriacao(LocalDateTime.now());
        orcRecusar.setDataExpiracao(LocalDateTime.now().plusDays(1));
        orcRecusar.setServicos(List.of(os));
        os.setOrcamento(orcRecusar);

        orcRecusar.recusar();
        assertEquals(StatusOrcamento.RECUSADO, orcRecusar.getStatus());
        assertEquals(StatusReservaEstoque.CANCELADO, item.getStatusReserva());
    }

    @Test
    void aplicarNovoStatus_invalido_deveLancar() {
        Orcamento orc = new Orcamento();
        orc.setTipoOrcamento(TipoOrcamento.INICIAL);
        orc.setDataCriacao(LocalDateTime.now());
        orc.setDataExpiracao(LocalDateTime.now().plusDays(1));

        assertThrows(RuntimeException.class, () -> orc.aplicarNovoStatus(StatusOrcamento.PENDENTE));
    }

    @Test
    void recalcularTotais_deveSomarMaoDeObraEItens() {
        OrcamentoItem item = new OrcamentoItem();
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("10.00"));

        Servico servico = new Servico(UUID.randomUUID(), "Serv", new BigDecimal("50.00"), 30);

        OrcamentoServico os = new OrcamentoServico();
        os.setMaoDeObra(new BigDecimal("20.00"));
        os.setServico(servico);
        os.setItens(List.of(item));
        item.setOrcamentoServico(os);

        Orcamento orc = new Orcamento();
        orc.setServicos(List.of(os));
        os.setOrcamento(orc);

        orc.recalcularTotais();
        assertEquals(new BigDecimal("20.00"), orc.getMaoObra());
        assertEquals(new BigDecimal("20.00"), orc.getSubtotalPecas());
        assertEquals(new BigDecimal("40.00"), orc.getTotal());
    }
}