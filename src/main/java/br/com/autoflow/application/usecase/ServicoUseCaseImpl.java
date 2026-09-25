package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.ServicoValidator;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.inbound.servico.*;
import br.com.autoflow.ports.outbound.ServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoUseCaseImpl implements
        CriarServicoUseCase,
        ListarServicosUseCase,
        BuscarServicoPorIdUseCase,
        AtualizarServicoUseCase,
        DeletarServicoUseCase {

    private final ServicoRepositoryPort servicoRepositoryPort;
    private final ServicoValidator servicoValidator;

    @Override
    @Transactional
    public Servico criar(Servico servico) {
        servicoValidator.validarCriacao(servico);
        return servicoRepositoryPort.save(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Servico> listarTodos(Pageable pageable) {
        return servicoRepositoryPort.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Servico buscarPorId(UUID id) {
        return servicoValidator.buscarPorId(id);
    }

    @Override
    @Transactional
    public Servico atualizar(UUID id, Servico servicoParam) {
        servicoValidator.validarAtualizacao(id, servicoParam);

        Servico servicoExistente = servicoValidator.buscarPorId(id);
        servicoExistente.atualizar(
                servicoParam.getDsServico(),
                servicoParam.getVlServico(),
                servicoParam.getQtTempoEstimadoMin()
        );

        return servicoRepositoryPort.save(servicoExistente);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        servicoValidator.validarExclusao(id);
        servicoRepositoryPort.deleteById(id);
    }
}