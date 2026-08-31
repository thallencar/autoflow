package br.com.autoflow.domain.model;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.domain.enums.*;
import br.com.autoflow.exception.RegraNegocioException;
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
    void deveGerarAlertaQuandoEstoqueBaixoEInsumo() {
        Estoque estoque = Estoque.builder()
                .quantidadeEstoque(5)
                .quantidadeMinima(10)
                .tipoCategoria(TipoItemEstoque.INSUMO)
                .build();

        assertTrue(estoque.deveDispararAlertaEstoqueBaixo());
        assertTrue(estoque.deveGerarAlertaEstoqueBaixo());
    }

    @Test
    void naoDeveGerarAlertaQuandoCategoriaNaoCritica() {
        Estoque estoque = Estoque.builder()
                .quantidadeEstoque(1)
                .quantidadeMinima(3)
                .tipoCategoria(TipoItemEstoque.PECA_COMPARTILHADA)
                .build();

        assertFalse(estoque.deveDispararAlertaEstoqueBaixo());
    }

    @Test
    void funcionarioDeveAlternarEstadoEOcupacao() {
        Funcionario funcionario = Funcionario.builder()
                .cpf("11144477735")
                .nome("Maria")
                .telefone("51999887766")
                .email("maria@email.com")
                .genero(Genero.FEMININO)
                .dataNascimento(LocalDate.of(1990, 3, 10))
                .cargo(Cargo.MECANICO)
                .build();

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
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        Veiculo veiculo = Veiculo.builder()
                .placa("ABC1234")
                .marca("Marca")
                .modelo("Modelo")
                .kmAtual(1000)
                .anoFabricacao((short) 2020)
                .cor("Azul")
                .cliente(cliente)
                .build();

        VeiculoRequest request = new VeiculoRequest("  ABC1A23  ", "Nova Marca", "Novo Modelo", 2000, (short) 2023, "Vermelho", cliente.getId());

        veiculo.atualizarDados(request, cliente);

        assertEquals("ABC1A23", veiculo.getPlaca());
        assertEquals("Nova Marca", veiculo.getMarca());
        assertEquals("Novo Modelo", veiculo.getModelo());
        assertEquals((short) 2023, veiculo.getAnoFabricacao());
        assertEquals("Vermelho", veiculo.getCor());
        assertSame(cliente, veiculo.getCliente());
    }

    @Test
    void orcamentoDeveRecalcularTotaisEAtualizarStatus() {
        OrcamentoItem item = OrcamentoItem.builder()
                .quantidade(2)
                .valorUnitario(new BigDecimal("15.50"))
                .statusReserva(StatusReservaEstoque.RESERVADO)
                .build();

        OrcamentoServico servico = OrcamentoServico.builder()
                .maoDeObra(new BigDecimal("30.00"))
                .itens(new ArrayList<>(List.of(item)))
                .build();

        item.setOrcamentoServico(servico);

        Orcamento orcamento = Orcamento.builder()
                .status(StatusOrcamento.PENDENTE)
                .tipoOrcamento(TipoOrcamento.INICIAL)
                .dataCriacao(LocalDateTime.now())
                .dataExpiracao(LocalDateTime.now().plusDays(2))
                .servicos(new ArrayList<>(List.of(servico)))
                .build();

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
        Orcamento orcamento = Orcamento.builder()
                .status(StatusOrcamento.APROVADO)
                .dataExpiracao(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(RegraNegocioException.class, orcamento::recusar);
        assertThrows(RegraNegocioException.class, () -> orcamento.aplicarNovoStatus(StatusOrcamento.RECUSADO));
    }

    @Test
    void ordemServicoDeveCarregarServicosAprovadosECalcularMetricas() {
        Servico servico = Servico.builder()
                .idServico(UUID.randomUUID())
                .dsServico("Balanceamento")
                .vlServico(new BigDecimal("120.00"))
                .qtTempoEstimadoMin(90)
                .build();

        OrcamentoItem item = OrcamentoItem.builder()
                .valorUnitario(new BigDecimal("20.00"))
                .quantidade(1)
                .build();
        item.calcularTotal();

        OrcamentoServico orcamentoServico = OrcamentoServico.builder()
                .servico(servico)
                .maoDeObra(new BigDecimal("50.00"))
                .itens(new ArrayList<>(List.of(item)))
                .build();
        item.setOrcamentoServico(orcamentoServico);

        Orcamento orcamento = Orcamento.builder()
                .status(StatusOrcamento.APROVADO)
                .servicos(new ArrayList<>(List.of(orcamentoServico)))
                .build();
        orcamentoServico.setOrcamento(orcamento);

        OrdemServico os = OrdemServico.builder()
                .statusOS(StatusOS.RECEBIDA)
                .dsRelatoCliente("Barulho na roda")
                .idCliente(UUID.randomUUID())
                .idVeiculo(UUID.randomUUID())
                .idsOrcamento(new ArrayList<>(List.of(orcamento)))
                .servicosExecucao(new ArrayList<>())
                .stPagamento(StatusPagamento.PENDENTE)
                .build();

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

        OrdemServico abandono = OrdemServico.builder()
                .statusOS(StatusOS.AGUARDANDO_APROVACAO)
                .dsRelatoCliente("Veículo parado")
                .idCliente(UUID.randomUUID())
                .idVeiculo(UUID.randomUUID())
                .dtFimDiagnostico(LocalDateTime.now().minusDays(3))
                .build();

        abandono.verificarAbandonoTecnico(2);
        assertEquals(StatusOS.ABANDONADO, abandono.getStatusOS());
    }

    @Test
    void ordemServicoDeveValidarEntregaEPrePersist() {
        OrdemServico os = OrdemServico.builder()
                .dsRelatoCliente("Cliente solicita revisão")
                .statusOS(StatusOS.FINALIZADA)
                .stPagamento(StatusPagamento.PAGO)
                .taxaPermanencia(null)
                .build();

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
        OrcamentoItem item = OrcamentoItem.builder()
                .quantidade(1)
                .valorUnitario(new BigDecimal("10.00"))
                .statusReserva(StatusReservaEstoque.RESERVADO)
                .build();

        OrcamentoServico servico = OrcamentoServico.builder()
                .maoDeObra(BigDecimal.ZERO)
                .itens(new ArrayList<>(List.of(item)))
                .build();

        item.setOrcamentoServico(servico);

        Orcamento orc = Orcamento.builder()
                .status(StatusOrcamento.PENDENTE)
                .servicos(new ArrayList<>(List.of(servico)))
                .dataCriacao(LocalDateTime.now().minusDays(5))
                .dataExpiracao(LocalDateTime.now().minusDays(1))
                .build();

        servico.setOrcamento(orc);

        orc.expirar();

        assertEquals(StatusOrcamento.CANCELADO, orc.getStatus());
        assertEquals(StatusReservaEstoque.CANCELADO, item.getStatusReserva());
        assertNotNull(orc.getDataDecisao());
    }
}
