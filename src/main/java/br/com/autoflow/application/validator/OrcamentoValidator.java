package br.com.autoflow.application.validator;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrcamentoValidator {

    private final OrdemServicoRepositoryPort ordemServicoRepositoryPort;
    private final EstoqueRepositoryPort estoqueRepositoryPort;
    private final ServicoValidator servicoValidator;
    private final OrcamentoRepositoryPort orcamentoRepositoryPort;

    public void validarCriacao(UUID idOs, Orcamento orcamento) {
        OrdemServico os = ordemServicoRepositoryPort.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOs));

        if (os.getStatusOS() == StatusOS.CANCELADA ||
                os.getStatusOS() == StatusOS.FINALIZADA ||
                os.getStatusOS() == StatusOS.ENTREGUE) {
            throw new RegraNegocioException(
                    String.format("Não é possível criar orçamento para uma Ordem de Serviço com status %s.", os.getStatusOS())
            );
        }

        List<Orcamento> orcamentosExistentes = orcamentoRepositoryPort.findByOrdemServicoIdOs(idOs);
        boolean temOrcamentoAnterior = orcamentosExistentes != null && !orcamentosExistentes.isEmpty();

        if (orcamento.getTipoOrcamento() == null) {
            throw new RegraNegocioException("O tipo de orçamento é obrigatório.");
        }

        boolean ehComplementar = orcamento.getTipoOrcamento().equals(TipoOrcamento.COMPLEMENTAR);

        if (ehComplementar) {
            if (!temOrcamentoAnterior) {
                throw new RegraNegocioException("Não é permitido criar um orçamento complementar sem antes existir um orçamento inicial para esta OS.");
            }
            List<UUID> servicosJaCadastrados = orcamentosExistentes.stream()
                    .filter(o -> o.getServicos() != null)
                    .flatMap(o -> o.getServicos().stream())
                    .map(s -> s.getServico().getIdServico())
                    .toList();

            if (orcamento.getServicos() != null) {
                for (var novoServico : orcamento.getServicos()) {
                    if (servicosJaCadastrados.contains(novoServico.getServico().getIdServico())) {
                        throw new RegraNegocioException(
                                String.format("O serviço com ID %s já foi adicionado em outro orçamento desta OS.", novoServico.getServico().getIdServico())
                        );
                    }
                }
            }
        } else {
            boolean jaExisteInicial = orcamentosExistentes != null && orcamentosExistentes.stream()
                    .anyMatch(o -> o.getTipoOrcamento() != null &&
                            o.getTipoOrcamento() == TipoOrcamento.INICIAL);
            if (jaExisteInicial) {
                throw new RegraNegocioException("Já existe um orçamento INICIAL cadastrado para esta Ordem de Serviço. Para adicionar novos itens, utilize o tipo COMPLEMENTAR.");
            }
            if (orcamento.getDataExpiracao() == null) {
                throw new RegraNegocioException("A data de expiração do orçamento é obrigatória.");
            }
            if (orcamento.getDataExpiracao().isBefore(LocalDateTime.now(ZoneId.systemDefault()))) {
                throw new RegraNegocioException("A data de expiração não pode ser anterior à data atual.");
            }
        }

        validarServicosDoOrcamento(orcamento);
    }

    public void validarAtualizacaoStatus(StatusOrcamento novoStatus) {
        if (novoStatus != StatusOrcamento.APROVADO && novoStatus != StatusOrcamento.RECUSADO) {
            throw new RegraNegocioException("O orçamento só pode ser alterado para APROVADO ou RECUSADO.");
        }
    }

    private void validarServicosDoOrcamento(Orcamento orcamento) {
        if (orcamento.getServicos() == null || orcamento.getServicos().isEmpty()) {
            throw new RegraNegocioException("O orçamento deve conter pelo menos um serviço.");
        }
        for (var servico : orcamento.getServicos()) {
            if (servico.getMaoDeObra() == null || servico.getMaoDeObra().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegraNegocioException("O valor da mão de obra deve ser maior que zero.");
            }
            if (servico.getItens() != null) {
                servico.getItens().forEach(item -> {
                    if (item.getIdEstoque() == null) {
                        throw new RegraNegocioException("O ID da peça/estoque é obrigatório no item.");
                    }
                    if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                        throw new RegraNegocioException("A quantidade de cada item/peça deve ser maior que zero.");
                    }
                    if (item.getValorUnitario() == null || item.getValorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new RegraNegocioException("O valor unitário do item/peça deve ser maior que zero.");
                    }
                });
            }
        }
    }

    public void validarEstoqueDisponivel(Orcamento orcamento) {
        if (orcamento.getServicos() == null) return;

        orcamento.getServicos().stream()
                .filter(s -> s.getItens() != null)
                .flatMap(s -> s.getItens().stream())
                .forEach(item -> {
                    var estoque = estoqueRepositoryPort.findById(item.getIdEstoque())
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