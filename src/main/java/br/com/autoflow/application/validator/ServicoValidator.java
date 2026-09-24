package br.com.autoflow.application.validator;

import br.com.autoflow.adapters.inbound.controller.dto.ServicoRequest;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.ports.outbound.OrcamentoServicoRepositoryPort;
import br.com.autoflow.ports.outbound.OsServicoRepositoryPort;
import br.com.autoflow.ports.outbound.ServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServicoValidator {

    private final ServicoRepositoryPort servicoRepositoryPort;
    private final OsServicoRepositoryPort osServicoRepositoryPort; // Porta de OS/Serviço correspondente
    private final OrcamentoServicoRepositoryPort orcamentoServicoRepositoryPort;

    public void validarCriacao(ServicoRequest request) {
        validarDescricaoDuplicada(request.dsServico());
    }

    public void validarAtualizacao(UUID id, ServicoRequest request) {
        validarExistencia(id);
        servicoRepositoryPort.findByDsServicoIgnoreCase(request.dsServico())
                .ifPresent(servicoExistente -> {
                    if (!servicoExistente.getIdServico().equals(id)) {
                        throw new RegraNegocioException("Já existe outro serviço cadastrado com a descrição: " + request.dsServico());
                    }
                });
    }

    public Servico buscarPorId(UUID id) {
        return servicoRepositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Servico ", id));
    }

    public void validarExclusao(UUID id) {
        validarExistencia(id);
        if (osServicoRepositoryPort.existsByServico_IdServico(id)) {
            throw new RegraNegocioException("Não é possível excluir este serviço pois ele já está vinculado a uma Ordem de Serviço.");
        }
        if (orcamentoServicoRepositoryPort.existsByServico_IdServico(id)) {
            throw new RegraNegocioException("Não é possível excluir este serviço pois ele já está vinculado a um Orçamento.");
        }
    }

    private void validarDescricaoDuplicada(String dsServico) {
        if (servicoRepositoryPort.existsByDsServicoIgnoreCase(dsServico)) {
            throw new RegraNegocioException("Já existe um serviço cadastrado com a descrição: " + dsServico);
        }
    }

    private void validarExistencia(UUID id) {
        if (!servicoRepositoryPort.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Serviço ", id);
        }
    }
}