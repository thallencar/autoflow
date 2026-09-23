package br.com.autoflow.application.usecase;

import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class OrcamentoExpiradoUseCaseTest {

    @Test
    void salvarOrcamentoExpirado_deveChamarRepositorioPort() {
        OrcamentoRepositoryPort repositoryPort = mock(OrcamentoRepositoryPort.class);

        OrcamentoExpiradoUseCase useCase = new OrcamentoExpiradoUseCase(repositoryPort);

        Orcamento orc = new Orcamento(
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        useCase.salvarOrcamentoExpirado(orc);
        verify(repositoryPort, times(1)).saveAndFlush(orc);
    }
}