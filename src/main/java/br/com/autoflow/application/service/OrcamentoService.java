package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.*;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrcamentoMapper;
import jakarta.persistence.EntityNotFoundException;
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
    private final EstoqueRepository estoqueRepository;

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        orcamentoValidator.validarCriacao(request);

        Orcamento orcamento = orcamentoMapper.toEntity(request);
        OrdemServico ordemServico = ordemServicoRepository.findById(request.idOs())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", request.idOs()));
        orcamento.setOrdemServico(ordemServico);

        if (orcamento.getServicos() != null) {
            for (var servico : orcamento.getServicos()) {
                servico.setOrcamento(orcamento);
                if (servico.getItens() != null) {
                    for (var item : servico.getItens()) {
                        item.setOrcamentoServico(servico);
                        item.setStatusReserva(StatusReservaEstoque.RESERVADO);
                    }
                }
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

        if (orcamento.getDataExpiracao() != null && LocalDateTime.now().isAfter(orcamento.getDataExpiracao())) {
            orcamento.expirar();
            orcamentoExpiradoService.salvarOrcamentoExpirado(orcamento);
            throw new RegraNegocioException("Não foi possível alterar o status: Este orçamento está expirado.");
        }

        if (request.status() == StatusOrcamento.APROVADO) {
            orcamentoValidator.validarEstoqueDisponivel(orcamento);
            deduzirItensDoEstoque(orcamento);
        }

        orcamento.aplicarNovoStatus(request.status());

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
    public void delete(UUID id) {
        if (!orcamentoRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Orçamento", id);
        }
        orcamentoRepository.deletarItensDiretosPorOrcamento(id);
        orcamentoRepository.deletarItensPorServicosDoOrcamento(id);
        orcamentoRepository.deletarServicosPorOrcamento(id);
    }

    private void deduzirItensDoEstoque(Orcamento orcamento) {
        if (orcamento.getServicos() == null) return;

        orcamento.getServicos().stream()
                .filter(servico -> servico.getItens() != null)
                .flatMap(servico -> servico.getItens().stream())
                .forEach(item -> {
                    Estoque estoque = estoqueRepository.findById(item.getIdEstoque())
                            .orElseThrow(() -> new EntidadeNaoEncontradaException("Item de Estoque", item.getIdEstoque()));

                    if (estoque.getQuantidadeEstoque() < item.getQuantidade()) {
                        throw new RegraNegocioException(
                                String.format("Saldo insuficiente para a peça %s no momento da aprovação.", estoque.getNomeItem())
                        );
                    }

                    estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - item.getQuantidade());
                    estoqueRepository.save(estoque);
                });
    }
}