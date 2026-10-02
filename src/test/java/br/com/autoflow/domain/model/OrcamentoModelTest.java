package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.enums.TipoOrcamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrcamentoModelTest {

    @Test
    @DisplayName("Deve aprovar o orçamento e alterar o status do item para VENDIDO, e recusar o orçamento alterando para CANCELADO")
    void aprovar_e_recusar_deveAtualizarStatusEItens() {
        UUID idEstoque = UUID.randomUUID();
        UUID idServico = UUID.randomUUID();
        UUID idOrcamento = UUID.randomUUID();

        // ==========================================
        // CENÁRIO 1: Testando a Aprovação
        // ==========================================
        OrcamentoItem itemAprovacao = new OrcamentoItem();
        itemAprovacao.setId(idEstoque);
        itemAprovacao.setQuantidade(1);
        itemAprovacao.setValorUnitario(new BigDecimal("10.00"));
        itemAprovacao.setValorTotal(new BigDecimal("10.00"));

        Servico servicoAprovacao = new Servico(idServico, "Serv", new BigDecimal("50.00"), 30);

        OrcamentoServico osAprovacao = new OrcamentoServico();
        osAprovacao.setMaoDeObra(new BigDecimal("20.00"));
        osAprovacao.setServico(servicoAprovacao);
        osAprovacao.setItens(List.of(itemAprovacao));
        itemAprovacao.setOrcamentoServico(osAprovacao);

        Orcamento orcamentoAprovacao = new Orcamento();
        orcamentoAprovacao.setId(idOrcamento);
        orcamentoAprovacao.setTipoOrcamento(TipoOrcamento.INICIAL);
        orcamentoAprovacao.setStatus(StatusOrcamento.PENDENTE);
        orcamentoAprovacao.setDataCriacao(LocalDateTime.now());
        orcamentoAprovacao.setDataExpiracao(LocalDateTime.now().plusDays(1));
        orcamentoAprovacao.setServicos(List.of(osAprovacao));
        osAprovacao.setOrcamento(orcamentoAprovacao);

        orcamentoAprovacao.aprovar();

        assertEquals(StatusOrcamento.APROVADO, orcamentoAprovacao.getStatus());
        assertEquals(StatusReservaEstoque.VENDIDO, itemAprovacao.getStatusReserva());

        // ==========================================
        // CENÁRIO 2: Testando a Recusa (Instâncias novas e independentes)
        // ==========================================
        OrcamentoItem itemRecusa = new OrcamentoItem();
        itemRecusa.setId(idEstoque);
        itemRecusa.setQuantidade(1);
        itemRecusa.setValorUnitario(new BigDecimal("10.00"));
        itemRecusa.setValorTotal(new BigDecimal("10.00"));
        itemRecusa.setStatusReserva(StatusReservaEstoque.RESERVADO); // Começa reservado

        Servico servicoRecusa = new Servico(idServico, "Serv", new BigDecimal("50.00"), 30);

        OrcamentoServico osRecusa = new OrcamentoServico();
        osRecusa.setMaoDeObra(new BigDecimal("20.00"));
        osRecusa.setServico(servicoRecusa);
        osRecusa.setItens(List.of(itemRecusa));
        itemRecusa.setOrcamentoServico(osRecusa);

        Orcamento orcamentoRecusa = new Orcamento();
        orcamentoRecusa.setId(idOrcamento);
        orcamentoRecusa.setTipoOrcamento(TipoOrcamento.INICIAL);
        orcamentoRecusa.setStatus(StatusOrcamento.PENDENTE); // Começa pendente
        orcamentoRecusa.setDataCriacao(LocalDateTime.now());
        orcamentoRecusa.setDataExpiracao(LocalDateTime.now().plusDays(1));
        orcamentoRecusa.setServicos(List.of(osRecusa));
        osRecusa.setOrcamento(orcamentoRecusa);

        orcamentoRecusa.recusar();

        assertEquals(StatusOrcamento.RECUSADO, orcamentoRecusa.getStatus());
        assertEquals(StatusReservaEstoque.CANCELADO, itemRecusa.getStatusReserva());
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