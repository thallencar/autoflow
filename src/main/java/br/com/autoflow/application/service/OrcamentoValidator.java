package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.OrdemServicoRepository; // Ajuste conforme seu pacote
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException; // Crie ou use sua exception de validação
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrcamentoValidator {

    private final OrdemServicoRepository ordemServicoRepository;

    public void validarCriacao(OrcamentoRequest request) {
        validarOrdemServico(request);
        validarDataExpiracao(request);
        validarValores(request);
        validarItens(request);
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

        boolean osExiste = ordemServicoRepository.existsById(request.idOs());
        if (!osExiste) {
            throw new EntidadeNaoEncontradaException("Ordem de Serviço", request.idOs());
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

    private void validarValores(OrcamentoRequest request) {
        if (request.maoObra() != null && request.maoObra().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O valor da mão de obra não pode ser negativo.");
        }

        if (request.subtotalPecas() != null && request.subtotalPecas().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O subtotal de peças não pode ser negativo.");
        }
    }

    private void validarItens(OrcamentoRequest request) {
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new RegraNegocioException("O orçamento deve conter pelo menos um item.");
        }

        request.itens().forEach(item -> {
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new RegraNegocioException("A quantidade de cada item deve ser maior que zero.");
            }
            if (item.valorUnitario() == null || item.valorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegraNegocioException("O valor unitário do item deve ser maior que zero.");
            }
        });
    }
}