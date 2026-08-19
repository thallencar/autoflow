package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarStatusOSRequest;
import br.com.autoflow.application.dto.MetricaOsResponse;
import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrdemServicoMapper;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final OrdemServicoMapper mapper;
    private final OrdemServicoValidator validator;
    private final FuncionarioRepository funcionarioRepository;

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarTodas() {
        List<OrdemServico> lista = repository.findAll();
        return mapper.toResponseList(lista);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));
        return mapper.toResponse(os);
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request, boolean possuiAgendamento) {
        Long carrosNoPatio = repository.countByStatusOSNot(StatusOS.ENTREGUE);
        validator.validarCriacao(request, possuiAgendamento, carrosNoPatio);
        OrdemServico os = mapper.toEntity(request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public OrdemServicoResponse atualizar(UUID id, OrdemServicoRequest request) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));

        validator.validarCliente(request.idCliente());
        validator.validarOrcamentosParaOS(request.idsOrcamento());
        mapper.updateEntityFromRequest(os, request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public void atualizarStatusPagamento(UUID id, StatusPagamento novoStatus) {
        OrdemServico ordemServico = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));
        validator.validarAtualizacaoPagamento(ordemServico, novoStatus);
        ordemServico.setStPagamento(novoStatus);
        repository.save(ordemServico);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Ordem de Serviço", id);
        }
        repository.deleteById(id);
    }
    @Transactional
    public OrdemServicoResponse atualizarStatus(UUID idOS, AtualizarStatusOSRequest request) {
        OrdemServico os = repository.findById(idOS)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOS));

        StatusOS novoStatus = request.status();
        if (novoStatus == StatusOS.EM_DIAGNOSTICO) {
            validator.validarAlocacaoMecanico(null, os.getIdFuncionario());
            Funcionario mecanico = funcionarioRepository.findById(os.getIdFuncionario())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário", os.getIdFuncionario()));
            mecanico.ocupar();
            funcionarioRepository.save(mecanico);
        }
        os.atualizarStatus(novoStatus, request.observacao());

        if (novoStatus == StatusOS.FINALIZADA || novoStatus == StatusOS.ENTREGUE || novoStatus == StatusOS.CANCELADA) {
            if (os.getIdFuncionario() != null) {
                funcionarioRepository.findById(os.getIdFuncionario()).ifPresent(mecanico -> {
                    mecanico.liberar();
                    funcionarioRepository.save(mecanico);
                });
            }
        }
        OrdemServico osSalva = repository.save(os);
        return mapper.toResponse(osSalva);
    }

    @Transactional(readOnly = true)
    public MetricaOsResponse obterMetricasPorOS(UUID idOs) {
        OrdemServico ordemServico = repository.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOs));
        return mapper.toMetricaResponse(ordemServico);
    }

    @Transactional(readOnly = true)
    public Page<MetricaOsResponse> buscarMetricasComFiltro(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusOS status,
            Pageable pageable) {
        Page<OrdemServico> ordens = repository.findMetricasComFiltro(dataInicio, dataFim, status, pageable);
        return ordens.map(mapper::toMetricaResponse);
    }
}