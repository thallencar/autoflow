package br.com.autoflow.application.service;

import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class OrcamentoExpiradoServiceTest {

    @Test
    void salvarOrcamentoExpirado_deveChamarRepositorio() {
        OrcamentoRepository repo = mock(OrcamentoRepository.class);
        OrcamentoExpiradoService svc = new OrcamentoExpiradoService(repo);

        Orcamento orc = new Orcamento();
        svc.salvarOrcamentoExpirado(orc);

        verify(repo, times(1)).saveAndFlush(orc);
    }
}