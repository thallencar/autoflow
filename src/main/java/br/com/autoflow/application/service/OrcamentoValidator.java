package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrcamentoItemRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoServicoRequest;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.domain.repository.EstoqueRepository;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrcamentoValidator {

    private final OrdemServicoRepository ordemServicoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ServicoValidator servicoValidator;
    private final OrcamentoRepository orcamentoRepository;

    public void validarCriacao(OrcamentoRequest request) {
        validarOrdemServico(request);

        List<Orcamento> orcamentosExistentes = orcamentoRepository.findByOrdemServicoIdOs(request.idOs());
        boolean temOrcamentoAnterior = orcamentosExistentes != null && !orcamentosExistentes.isEmpty();

        boolean ehComplementar = request.tipoOrcamento() != null &&
                request.tipoOrcamento().equals(TipoOrcamento.COMPLEMENTAR);

        if (ehComplementar) {
            if (!temOrcamentoAnterior) {
                throw new RegraNegocioException("Não é permitido criar um orçamento complementar sem antes existir um orçamento inicial para esta OS.");
            }

            List<UUID> servicosJaCadastrados = orcamentosExistentes.stream()
                    .filter(o -> o.getServicos() != null)
                    .flatMap(o -> o.getServicos().stream())
                    .map(s -> s.getServico().getIdServico())
                    .toList();

            if (request.servicos() != null) {
                for (var novoServico : request.servicos()) {
                    if (servicosJaCadastrados.contains(novoServico.idServico())) {
                        throw new RegraNegocioException(
                                String.format("O serviço com ID %s já foi adicionado em outro orçamento desta OS.", novoServico.idServico())
                        );
                    }
                }
            }
        } else {
            validarDataExpiracao(request);
        }

        validarServicosRequest(request.servicos());
    }

    public void validarAtualizacaoStatus(StatusOrcamento novoStatus) {
        if (novoStatus != StatusOrcamento.APROVADO && novoStatus != StatusOrcamento.RECUSADO) {
            throw new RegraNegocioException("O orçamento só pode ser alterado para APROVADO ou RECUSADO.");
        }
    }

    private void validarOrdemServico(OrcamentoRequest request) {
        if (request.idOs() == null) {
            throw new RegraNegocioException("O ID da Ordem de Serviço é obrigatório para criar um orçamento.");
        }

        OrdemServico os = ordemServicoRepository.findById(request.idOs())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", request.idOs()));

        if (os.getStatusOS() == StatusOS.CANCELADA ||
                os.getStatusOS() == StatusOS.FINALIZADA ||
                os.getStatusOS() == StatusOS.ENTREGUE) {
            throw new RegraNegocioException(
                    String.format("Não é possível criar orçamento para uma Ordem de Serviço com status %s.", os.getStatusOS())
            );
        }
    }

    private void validarDataExpiracao(OrcamentoRequest request) {
        if (request.dataExpiracao() == null) {
            throw new RegraNegocioException("A data de expiração do orçamento é obrigatória.");
        }
        if (request.dataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("A data de expiração não pode ser anterior à data atual.");
        }
    }

    public void validarServicosRequest(List<OrcamentoServicoRequest> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new RegraNegocioException("O orçamento deve conter pelo menos um serviço.");
        }
        for (OrcamentoServicoRequest servico : servicos) {
            validarEBuscarServico(servico);
            if (servico.itens() != null) {
                validarItensDoServico(servico.itens());
            }
        }
    }

    public Servico validarEBuscarServico(OrcamentoServicoRequest request) {
        if (request.idServico() == null) {
            throw new RegraNegocioException("O ID do serviço é obrigatório.");
        }
        if (request.maoDeObra() == null || request.maoDeObra().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da mão de obra deve ser maior que zero.");
        }
        return servicoValidator.buscarPorId(request.idServico());
    }

    private void validarItensDoServico(List<OrcamentoItemRequest> itens) {
        itens.forEach(item -> {
            if (item.idEstoque() == null) {
                throw new RegraNegocioException("O ID da peça/estoque é obrigatório no item.");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new RegraNegocioException("A quantidade de cada item/peça deve ser maior que zero.");
            }
            if (item.valorUnitario() == null || item.valorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegraNegocioException("O valor unitário do item/peça deve ser maior que zero.");
            }
            Estoque itemEstoque = estoqueRepository.findById(item.idEstoque())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Item de Estoque", item.idEstoque()));

            if (itemEstoque.getQuantidadeEstoque() < item.quantidade()) {
                throw new RegraNegocioException(
                        String.format("Estoque insuficiente para a peça '%s'. Solicitado: %d, Disponível: %d.",
                                itemEstoque.getNomeItem(), item.quantidade(), itemEstoque.getQuantidadeEstoque())
                );
            }
        });
    }

    public void validarEstoqueDisponivel(Orcamento orcamento) {
        if (orcamento.getServicos() == null) return;

        orcamento.getServicos().stream()
                .filter(servico -> servico.getItens() != null)
                .flatMap(servico -> servico.getItens().stream())
                .forEach(item -> {
                    Estoque estoque = estoqueRepository.findById(item.getIdEstoque())
                            .orElseThrow(() -> new EntidadeNaoEncontradaException("Item de Estoque", item.getIdEstoque()));

                    if (estoque.getQuantidadeEstoque() < item.getQuantidade()) {
                        throw new RegraNegocioException(
                                String.format("Saldo insuficiente no estoque para a peça '%s'. Solicitado: %d, Disponível: %d.",
                                        estoque.getNomeItem(), item.getQuantidade(), estoque.getQuantidadeEstoque())
                        );
                    }
                });
    }
}