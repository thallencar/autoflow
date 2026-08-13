package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.OrcamentoRepository;
import br.com.autoflow.domain.repository.VeiculoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemServicoValidator {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrcamentoRepository orcamentoRepository;

    private static final int CAPACIDADE_MAXIMA_PATIO = 10;

    public void validarCriacao(OrdemServicoRequest request, String placaVeiculo, boolean possuiAgendamento, Long carrosNoPatioAtual) {
        validarCliente(request.idCliente());
        validarVeiculoPorPlaca(placaVeiculo);
        validarOrcamentoParaOS(request.idOrcamento());
        validarCapacidadePatioEAgendamento(possuiAgendamento, carrosNoPatioAtual);
    }

    public void validarCliente(UUID idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new EntidadeNaoEncontradaException("Cliente", idCliente);
        }
    }

    public void validarVeiculoPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new RegraNegocioException("A placa do veículo é obrigatória.");
        }
        boolean existe = veiculoRepository.existsByPlaca(placa.toUpperCase());
        if (!existe) {
            throw new RegraNegocioException("Veículo com a placa " + placa + " não foi encontrado.");
        }
    }

    public void validarOrcamentoParaOS(UUID idOrcamento) {
        Orcamento orcamento = orcamentoRepository.findById(idOrcamento)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", idOrcamento));

        if (!"Aprovado".equalsIgnoreCase(orcamento.getStatus())) {
            throw new RegraNegocioException(
                    String.format("A Ordem de Serviço não pode ser iniciada. O Orçamento precisa estar 'Aprovado' (Status atual: %s).", orcamento.getStatus())
            );
        }
        if (orcamento.getDataExpiracao() != null && LocalDateTime.now().isAfter(orcamento.getDataExpiracao())) {
            throw new RegraNegocioException("O orçamento informado está expirado. Solicite uma atualização do orçamento.");
        }
    }

    private void validarCapacidadePatioEAgendamento(boolean possuiAgendamento, Long carrosNoPatioAtual) {
        if (!possuiAgendamento && carrosNoPatioAtual >= CAPACIDADE_MAXIMA_PATIO) {
            throw new RegraNegocioException(
                    "O pátio está com capacidade máxima (" + CAPACIDADE_MAXIMA_PATIO + " veículos) e a equipe sobrecarregada. " +
                            "Não estamos recebendo veículos sem agendamento no momento."
            );
        }
    }
}