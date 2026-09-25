package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.*;
import br.com.autoflow.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BusinessRulesModelTest {

    @Test
    void deveGerarAlertaКогдаEstoqueBaixoEInsumo() {
        Estoque estoque = new Estoque(
                UUID.randomUUID(), "Item Teste", "Marca Teste", new BigDecimal("10.00"),
                5, 10, TipoItemEstoque.INSUMO
        );

        assertTrue(estoque.deveDispararAlertaEstoqueBaixo());
    }

    @Test
    void naoDeveGerarAlertaQuandoCategoriaNaoCritica() {
        Estoque estoque = new Estoque(
                UUID.randomUUID(), "Item Teste", "Marca Teste", new BigDecimal("10.00"),
                1, 3, TipoItemEstoque.PECA
        );

        assertFalse(estoque.deveDispararAlertaEstoqueBaixo());
    }

    @Test
    void funcionarioDeveAlternarEstadoEOcupacao() {
        Funcionario funcionario = new Funcionario(
                UUID.randomUUID(), "11144477735", "Maria", "51999887766",
                "maria@email.com", Genero.FEMININO, LocalDate.of(1990, 3, 10),
                Cargo.MECANICO, null, false,0
        );

        funcionario.ocupar();
        assertTrue(funcionario.isOcupado());

        funcionario.liberar();
        assertFalse(funcionario.isOcupado());

        funcionario.adicionarAdvertencia();
        funcionario.adicionarAdvertencia();
        funcionario.adicionarAdvertencia();

        assertTrue(funcionario.deveSerDemitido());
    }

    @Test
    void veiculoDeveAtualizarDadosComFormatacaoECliente() {
        UUID clienteId = UUID.randomUUID();

        Veiculo veiculo = new Veiculo(
                UUID.randomUUID(),           // id
                "ABC1234",                   // placa
                "Marca",                     // marca
                "Modelo",                    // modelo
                1000,                        // kmAtual (Integer)
                (short) 2020,                // anoFabricacao (Short)
                "Azul",                      // cor
                clienteId                    // clienteId (UUID)
        );

        String novaPlaca = "  ABC1A23  ";
        String novaMarca = "Nova Marca";
        String novoModelo = "Novo Modelo";
        Integer novoKm = 2000;
        Short novoAno = 2023;
        String novaCor = "Vermelho";

        veiculo.atualizar(novaPlaca, novaMarca, novoModelo, novoKm, novoAno, novaCor);

        assertEquals("ABC1A23", veiculo.getPlaca()); // Verifica se limpou e formatou a placa
        assertEquals("Nova Marca", veiculo.getMarca());
        assertEquals("Novo Modelo", veiculo.getModelo());
        assertEquals((short) 2023, veiculo.getAnoFabricacao());
        assertEquals("Vermelho", veiculo.getCor());
        assertEquals(clienteId, veiculo.getClienteId());
    }

    @Test
    void orcamentoDeveRecalcularTotaisEAtualizarStatus() {
        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), StatusReservaEstoque.RESERVADO, 2,
                new BigDecimal("15.50"), null, UUID.randomUUID(), null, null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(), new BigDecimal("30.00"), null,
                new ArrayList<>(List.of(item)), null
        );

        item.setOrcamentoServico(servico);

        Orcamento orcamento = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now(), LocalDateTime.now().plusDays(2), null,
                null, null, null, null, new ArrayList<>(List.of(servico)), null
        );

        servico.setOrcamento(orcamento);
        orcamento.recalcularTotais();

        assertEquals(new BigDecimal("31.00"), orcamento.getSubtotalPecas());
        assertEquals(new BigDecimal("30.00"), orcamento.getMaoObra());
        assertEquals(new BigDecimal("61.00"), orcamento.getTotal());
        assertEquals(new BigDecimal("31.00"), item.getValorTotal());

        orcamento.aprovar();

        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        assertEquals(StatusReservaEstoque.VENDIDO, item.getStatusReserva());
        assertNotNull(orcamento.getDataDecisao());
    }

    @Test
    void orcamentoNaoDevePermitirAlteracaoDeStatusInvalida() {
        Orcamento orcamento = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.APROVADO,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null,
                null, null, null, null, null, null
        );

        assertThrows(RegraNegocioException.class, orcamento::recusar);
        assertThrows(RegraNegocioException.class, () -> orcamento.aplicarNovoStatus(StatusOrcamento.RECUSADO));
    }

    @Test
    void ordemServicoDeveCarregarServicosAprovadosECalcularMetricas() {
        Servico servico = new Servico(
                UUID.randomUUID(), "Balanceamento", new BigDecimal("120.00"), 90
        );

        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), null, 1, new BigDecimal("20.00"),
                null, UUID.randomUUID(), null, null
        );
        item.calcularTotal();

        OrcamentoServico orcamentoServico = new OrcamentoServico(
                UUID.randomUUID(), new BigDecimal("50.00"), servico,
                new ArrayList<>(List.of(item)), null
        );
        item.setOrcamentoServico(orcamentoServico);

        Orcamento orcamento = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.APROVADO,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null,
                null, null, null, null, new ArrayList<>(List.of(orcamentoServico)), null
        );
        orcamentoServico.setOrcamento(orcamento);

        OrdemServico os = new OrdemServico(
                UUID.randomUUID(),             // idOs
                StatusOS.RECEBIDA,             // statusOS
                "Barulho na roda",             // dsRelatoCliente
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
                StatusPagamento.PENDENTE,      // stPagamento
                null,                          // dsMotivoCancelamento
                null,                          // taxaPermanencia
                UUID.randomUUID(),             // idCliente
                UUID.randomUUID(),             // idVeiculo
                null,                          // idFuncionario
                new ArrayList<>(List.of(orcamento)), // idsOrcamento
                new ArrayList<>()              // servicosExecucao
        );

        os.carregarServicosDosOrcamentosAprovados();
        assertEquals(1, os.getServicosExecucao().size());

        orcamento.setStatus(StatusOrcamento.PENDENTE);
        os.setStatusOS(StatusOS.RECEBIDA);
        os.atualizarStatus(StatusOS.EM_DIAGNOSTICO, "Diagnóstico inicial");
        os.setStatusOS(StatusOS.EM_DIAGNOSTICO);
        os.atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "Conclusão");

        os.setDataInicioExecucao(LocalDateTime.now().minusMinutes(120));
        os.setDataFimExecucao(LocalDateTime.now().minusMinutes(30));
        assertEquals(90L, os.getTempoTotalExecucaoMinutos());
        assertEquals(90, os.getTempoTotalEstimadoMinutos());
        assertEquals(0L, os.getDiferencaMinutos());

        os.setDtFimDiagnostico(LocalDateTime.now().minusDays(10));
        os.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);
        os.verificarCancelamentoAutomatico(5, new BigDecimal("15.00"));
        assertEquals(StatusOS.CANCELADA, os.getStatusOS());

        OrdemServico abandono = new OrdemServico(
                UUID.randomUUID(),             // idOs
                StatusOS.RECEBIDA,             // statusOS
                "Barulho na roda",             // dsRelatoCliente
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
                StatusPagamento.PENDENTE,      // stPagamento
                null,                          // dsMotivoCancelamento
                null,                          // taxaPermanencia
                UUID.randomUUID(),             // idCliente
                UUID.randomUUID(),             // idVeiculo
                null,                          // idFuncionario
                new ArrayList<>(List.of(orcamento)), // idsOrcamento
                new ArrayList<>()              // servicosExecucao
        );

        abandono.verificarAbandonoTecnico(2);
        assertEquals(StatusOS.ABANDONADO, abandono.getStatusOS());
    }

    @Test
    void ordemServicoDeveValidarEntregaEPrePersist() {
        // Criando um orçamento mockado para satisfazer a regra de validação de requisitos
        Orcamento orcamento = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.APROVADO,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null,
                null, null, null, null, new ArrayList<>(), null
        );

        OrdemServico os = new OrdemServico(
                UUID.randomUUID(),             // idOs
                StatusOS.FINALIZADA,           // statusOS
                "Cliente solicita revisão",    // dsRelatoCliente
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
                StatusPagamento.PAGO,          // stPagamento (Deve ser PAGO para permitir a entrega)
                null,                          // dsMotivoCancelamento
                null,                          // taxaPermanencia
                UUID.randomUUID(),             // idCliente
                UUID.randomUUID(),             // idVeiculo
                null,                          // idFuncionario
                new ArrayList<>(List.of(orcamento)), // idsOrcamento (Vinculado para passar na validação)
                new ArrayList<>()              // servicosExecucao
        );

        os.prePersist();
        assertEquals(BigDecimal.ZERO, os.getTaxaPermanencia());

        os.setDataInicioExecucao(LocalDateTime.now().minusMinutes(40));
        os.setDataFimExecucao(LocalDateTime.now());
        os.setStatusOS(StatusOS.FINALIZADA);
        os.atualizarStatus(StatusOS.ENTREGUE, null);

        assertEquals(StatusOS.ENTREGUE, os.getStatusOS());
    }

    @Test
    void orcamentoExpirarDeveCancelarEAtualizarItens() {
        OrcamentoItem item = new OrcamentoItem(
                UUID.randomUUID(), StatusReservaEstoque.RESERVADO, 1,
                new BigDecimal("10.00"), null, UUID.randomUUID(), null, null
        );

        OrcamentoServico servico = new OrcamentoServico(
                UUID.randomUUID(), BigDecimal.ZERO, null,
                new ArrayList<>(List.of(item)), null
        );

        item.setOrcamentoServico(servico);

        Orcamento orc = new Orcamento(
                UUID.randomUUID(), TipoOrcamento.INICIAL, StatusOrcamento.PENDENTE,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1), null,
                null, null, null, null, new ArrayList<>(List.of(servico)), null
        );

        servico.setOrcamento(orc);

        orc.expirar();

        assertEquals(StatusOrcamento.CANCELADO, orc.getStatus());
        assertEquals(StatusReservaEstoque.CANCELADO, item.getStatusReserva());
        assertNotNull(orc.getDataDecisao());
    }
}