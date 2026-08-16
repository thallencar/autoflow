package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrcamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoMapper orcamentoMapper;
    private final OrcamentoValidator orcamentoValidator;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoExpiradoService orcamentoExpiradoService;

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        orcamentoValidator.validarCriacao(request);

        Orcamento orcamento = orcamentoMapper.toEntity(request);
        OrdemServico ordemServico = ordemServicoRepository.findById(request.idOs())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", request.idOs()));
        orcamento.setOrdemServico(ordemServico);

        if (orcamento.getItens() != null) {
            for (var item : orcamento.getItens()) {
                item.setOrcamento(orcamento);
                item.setStatusReserva(StatusReservaEstoque.RESERVADO);
            }
        }
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento = orcamentoRepository.save(orcamento);
        return orcamentoMapper.toResponse(orcamento);
    }

    @Transactional
    public OrcamentoResponse atualizarStatus(UUID id, AtualizarStatusOrcamentoRequest request) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", id));

        orcamentoValidator.validarAtualizacaoStatus(request.status());

        try {
            orcamento.aplicarNovoStatus(request.status());

        } catch (RegraNegocioException e) {
            if (orcamento.getStatus() == StatusOrcamento.CANCELADO) {
                orcamentoExpiradoService.salvarOrcamentoExpirado(orcamento);
            }
            throw e;
        }
        orcamento = orcamentoRepository.save(orcamento);
        return orcamentoMapper.toResponse(orcamento);
    }


    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarTodos() {
        return orcamentoRepository.findAll().stream()
                .map(orcamentoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(UUID id) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", id));
        return orcamentoMapper.toResponse(orcamento);
    }
    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarPorOrdemServico(UUID idOs) {
        List<Orcamento> orcamientos = orcamentoRepository.findByOrdemServicoIdOs(idOs);
        if (orcamientos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum orçamento encontrado para a Ordem de Serviço ID: ", idOs);
        }
        return orcamientos.stream()
                .map(orcamentoMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete (UUID id){
        if(!orcamentoRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Orçamento", id);
        }
        orcamentoRepository.deleteById(id);
    }
}