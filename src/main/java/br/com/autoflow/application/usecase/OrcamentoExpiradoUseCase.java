package br.com.autoflow.application.usecase;

import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrcamentoExpiradoUseCase {

    private final OrcamentoRepositoryPort orcamentoRepositoryPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvarOrcamentoExpirado(Orcamento orcamento) {
        orcamentoRepositoryPort.saveAndFlush(orcamento);
    }
}