package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrcamentoServicoRequest;
import br.com.autoflow.application.dto.ServicoRequest;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.domain.repository.OrcamentoServicoRepository;
import br.com.autoflow.domain.repository.OsServicoRepository;
import br.com.autoflow.domain.repository.ServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServicoValidator {

    private final ServicoRepository servicoRepository;
    private final OsServicoRepository osServicoRepository;
    private final OrcamentoServicoRepository orcamentoServicoRepository;

    public void validarCriacao(ServicoRequest request) {
        validarDescricaoDuplicada(request.dsServico());
    }

    public void validarAtualizacao(UUID id, ServicoRequest request) {
        validarExistencia(id);
        servicoRepository.findByDsServicoIgnoreCase(request.dsServico())
                .ifPresent(servicoExistente -> {
                    if (!servicoExistente.getIdServico().equals(id)) {
                        throw new RegraNegocioException("Já existe outro serviço cadastrado com a descrição: " + request.dsServico());
                    }
                });
    }
    public Servico buscarPorId(UUID id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Servico ", id));
    }

    public void validarExclusao(UUID id) {
        validarExistencia(id);
        if (osServicoRepository.existsByServico_IdServico(id)) {
            throw new RegraNegocioException("Não é possível excluir este serviço pois ele já está vinculado a uma Ordem de Serviço.");
        }
        if (orcamentoServicoRepository.existsByServico_IdServico(id)) {
            throw new RegraNegocioException("Não é possível excluir este serviço pois ele já está vinculado a um Orçamento.");
        }
    }

    private void validarDescricaoDuplicada(String dsServico) {
        if (servicoRepository.existsByDsServicoIgnoreCase(dsServico)) {
            throw new RegraNegocioException("Já existe um serviço cadastrado com a descrição: " + dsServico);
        }
    }

    private void validarExistencia(UUID id) {
        if (!servicoRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Serviço " ,id);
        }
    }
}