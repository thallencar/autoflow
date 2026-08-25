package br.com.autoflow.application.service;

import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrcamentoExpiradoService {

    private final OrcamentoRepository orcamentoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvarOrcamentoExpirado(Orcamento orcamento) {
        orcamentoRepository.saveAndFlush(orcamento);
    }
}