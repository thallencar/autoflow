package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoModelTest {

    @Test
    void prePersist_deveInicializarCamposPadrao() {
        OrdemServico os = new OrdemServico();
        os.prePersist();
        assertNotNull(os.getDtAberturaOs());
        assertNotNull(os.getStPagamento());
        assertNotNull(os.getTaxaPermanencia());
    }

    @Test
    void carregarServicosDosOrcamentosAprovados_deveAdicionarServico() {
        Servico serv = new Servico(
                UUID.randomUUID(),
                "S",
                new BigDecimal("10.00"),
                30
        );

        // Usando o construtor vazio + setters para Orcamento
        Orcamento orc = new Orcamento();
        orc.setId(UUID.randomUUID());
        orc.setTipoOrcamento(TipoOrcamento.INICIAL);
        orc.setStatus(StatusOrcamento.APROVADO);
        orc.setDataCriacao(LocalDateTime.now());
        orc.setDataExpiracao(LocalDateTime.now().plusDays(1));

        OrcamentoServico osServ = new OrcamentoServico(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                serv,
                List.of(),
                orc
        );

        orc.setServicos(List.of(osServ));

        OrdemServico ordem = new OrdemServico();
        ordem.setIdsOrcamento(List.of(orc));

        ordem.carregarServicosDosOrcamentosAprovados();
        assertFalse(ordem.getServicosExecucao().isEmpty());
    }

    @Test
    void verificarCancelamentoAutomatico_deveCancelarECalcularTaxa() {
        OrdemServico ordem = new OrdemServico();
        ordem.setStatusOS(StatusOS.AGUARDANDO_APROVACAO);
        ordem.setDtFimDiagnostico(LocalDateTime.now().minusDays(10));

        ordem.verificarCancelamentoAutomatico(5, new BigDecimal("10.00"));

        assertEquals(StatusOS.CANCELADA, ordem.getStatusOS());
        assertNotNull(ordem.getDtEncerramentoOs());
        assertTrue(ordem.getTaxaPermanencia().compareTo(BigDecimal.ZERO) > 0);
    }
}