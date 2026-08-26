package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
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
        Servico serv = Servico.builder().idServico(UUID.randomUUID()).dsServico("S").vlServico(new BigDecimal("10.00")).build();
        OrcamentoServico osServ = OrcamentoServico.builder().servico(serv).build();
        Orcamento orc = Orcamento.builder().status(StatusOrcamento.APROVADO).servicos(List.of(osServ)).build();
        osServ.setOrcamento(orc);

        OrdemServico ordem = OrdemServico.builder().idsOrcamento(List.of(orc)).build();
        ordem.carregarServicosDosOrcamentosAprovados();
        assertFalse(ordem.getServicosExecucao().isEmpty());
    }

    @Test
    void verificarCancelamentoAutomatico_deveCancelarECalcularTaxa() {
        OrdemServico ordem = OrdemServico.builder().statusOS(StatusOS.AGUARDANDO_APROVACAO).dtFimDiagnostico(LocalDateTime.now().minusDays(10)).build();
        ordem.verificarCancelamentoAutomatico(5, new BigDecimal("10.00"));
        assertEquals(StatusOS.CANCELADA, ordem.getStatusOS());
        assertNotNull(ordem.getDtEncerramentoOs());
        assertTrue(ordem.getTaxaPermanencia().compareTo(BigDecimal.ZERO) > 0);
    }
}
