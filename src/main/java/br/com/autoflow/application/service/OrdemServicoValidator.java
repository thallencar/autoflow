package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.*;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemServicoValidator {

    private static final int CAPACIDADE_MAXIMA_PATIO = 15;

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final FuncionarioRepository funcionarioRepository;

    public void validarCriacao(OrdemServicoRequest request, boolean possuiAgendamento, Long carrosNoPatioAtual) {
        validarCliente(request.idCliente());
        validarVeiculoPorID(request.idVeiculo());
        validarFuncionarioID(request.idFuncionario());

        validarPropriedadeVeiculo(request.idCliente(), request.idVeiculo());
        validarKmEntrada(request.idVeiculo(), request.nrKmEntrada());
        validarTermoDeAceite(request.stTermoAceito());
        validarDataAceiteTermo(request.stTermoAceito(), request.dtAceiteTermo());

        validarOrcamentosParaOS(request.idsOrcamento());
        validarCapacidadePatioEAgendamento(possuiAgendamento, carrosNoPatioAtual);

        boolean existeOsAberta = ordemServicoRepository
                .existsByIdVeiculoAndStatusOSNotIn(
                        request.idVeiculo(),
                        List.of(StatusOS.FINALIZADA, StatusOS.CANCELADA)
                );
        if (existeOsAberta) {
            throw new RegraNegocioException("Já existe uma Ordem de Serviço em andamento para este veículo.");
        }
    }

    public void validarCliente(UUID idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new EntidadeNaoEncontradaException("Cliente", idCliente);
        }
    }

    public void validarFuncionarioID(UUID funcionarioID) {
        if (!funcionarioRepository.existsById(funcionarioID)) {
            throw new EntidadeNaoEncontradaException("Funcionário", funcionarioID);
        }
    }

    public void validarVeiculoPorID(UUID veiculoId) {
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new EntidadeNaoEncontradaException("Veículo", veiculoId);
        }
    }

    public void validarTermoDeAceite(boolean termo) {
        if (!termo) {
            throw new RegraNegocioException("O Termo de aceite precisa estar assinado para continuar com a OS.");
        }
    }

    public List<Orcamento> validarECarregarOrcamentosParaOS(List<UUID> idsOrcamento) {
        if (idsOrcamento == null || idsOrcamento.isEmpty()) {
            throw new RegraNegocioException("A Ordem de Serviço deve ter ao menos um orçamento associado.");
        }
        return idsOrcamento.stream()
                .map(this::validarOrcamentoParaOS)
                .toList();
    }
    public void validarOrcamentosParaOS(List<UUID> idsOrcamento) {
        validarECarregarOrcamentosParaOS(idsOrcamento);
    }

    public Orcamento validarOrcamentoParaOS(UUID idOrcamento) {
        Orcamento orcamento = orcamentoRepository.findById(idOrcamento)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", idOrcamento));

        if (!"Aprovado".equalsIgnoreCase(orcamento.getStatus())) {
            throw new RegraNegocioException(
                    String.format("A Ordem de Serviço não pode ser iniciada. O Orçamento (ID: %s) precisa estar 'Aprovado' (Status atual: %s).",
                            idOrcamento, orcamento.getStatus())
            );
        }
        if (orcamento.getDataExpiracao() != null && LocalDateTime.now().isAfter(orcamento.getDataExpiracao())) {
            throw new RegraNegocioException(
                    String.format("O orçamento (ID: %s) está expirado. Solicite uma atualização.", idOrcamento)
            );
        }
        validarOrcamentoSemOS(idOrcamento);
        return orcamento;
    }

    public void validarOrcamentoSemOS(UUID idOrcamento) {
        boolean orcamentoJaUtilizado = orcamentoRepository.existsByIdAndIdOsIsNotNull(idOrcamento);
        if (orcamentoJaUtilizado) {
            throw new RegraNegocioException(
                    String.format("O orçamento (ID: %s) já está vinculado a outra Ordem de Serviço.", idOrcamento)
            );
        }
    }

    private void validarCapacidadePatioEAgendamento(boolean possuiAgendamento, Long carrosNoPatioAtual) {
        if (!possuiAgendamento) {
            throw new RegraNegocioException("Não é permitido criar Ordem de Serviço sem agendamento prévio.");
        }
        if (carrosNoPatioAtual >= CAPACIDADE_MAXIMA_PATIO) {
            throw new RegraNegocioException(
                    String.format("O pátio atingiu a capacidade máxima de %d veículos. Não é possível abrir novas Ordens de Serviço no momento.", CAPACIDADE_MAXIMA_PATIO)
            );
        }
    }

    public void validarPropriedadeVeiculo(UUID idCliente, UUID idVeiculo) {
        boolean veiculoPertenceAoCliente = veiculoRepository.existsByIdAndClienteId(idVeiculo, idCliente);
        if (!veiculoPertenceAoCliente) {
            throw new RegraNegocioException("O veículo informado não pertence ao cliente cadastrado na OS.");
        }
    }

    public void validarKmEntrada(UUID idVeiculo, Integer kmEntradaAtual) {
        if (kmEntradaAtual != null) {
            if (kmEntradaAtual < 0) {
                throw new RegraNegocioException("A quilometragem de entrada não pode ser negativa.");
            }
            ordemServicoRepository.findTopByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo)
                    .ifPresent(ultimaOs -> {
                        if (ultimaOs.getNrKmEntrada() != null && kmEntradaAtual < ultimaOs.getNrKmEntrada()) {
                            throw new RegraNegocioException(
                                    String.format("A quilometragem informada (%d km) não pode ser menor que a última registrada na OS anterior (%d km).",
                                            kmEntradaAtual, ultimaOs.getNrKmEntrada())
                            );
                        }
                    });
        }
    }

    public void validarDataAceiteTermo(Boolean termoAceito, LocalDateTime dtAceite) {
        if (Boolean.TRUE.equals(termoAceito)) {
            if (dtAceite == null) {
                throw new RegraNegocioException("A data do aceite do termo deve ser informada quando o termo for assinado.");
            }
            if (dtAceite.isAfter(LocalDateTime.now())) {
                throw new RegraNegocioException("A data do aceite do termo não pode estar no futuro.");
            }
        }
    }
}