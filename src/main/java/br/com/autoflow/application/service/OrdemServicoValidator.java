//package br.com.autoflow.domain.validation;
//
//import br.com.autoflow.application.dto.OrdemServicoRequest;
//import br.com.autoflow.domain.Orcamento;
//import br.com.autoflow.domain.enums.StatusOrcamento;
//import br.com.autoflow.domain.repository.ClienteRepository;
//import br.com.autoflow.domain.repository.OrcamentoRepository;
//import br.com.autoflow.domain.repository.VeiculoRepository;
//import br.com.autoflow.exception.RegraDeNegocioException;
//import br.com.autoflow.exception.EntidadeNaoEncontradaException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//public class OrdemServicoValidator {
//
//    private final ClienteRepository clienteRepository;
//    private final VeiculoRepository veiculoRepository;
//    private final OrcamentoRepository orcamentoRepository;
//
//    // Supondo um limite padrão de vagas no pátio da oficina
//    private static final int CAPACIDADE_MAXIMA_PATIO = 15;
//
//    /**
//     * Validações executadas na criação de uma nova Ordem de Serviço
//     */
//    public void validarCriacao(OrdemServicoRequest request, String placaVeiculo, boolean possuiAgendamento, int carrosNoPatioAtual) {
//
//        // 1. Validar existência do Cliente (Quem trouxe o carro)
//        validarCliente(request.idCliente());
//
//        // 2. Validar existência do Veículo pela Placa
//        validarVeiculoPorPlaca(placaVeiculo);
//
//        // 3. Validar se o Orçamento existe e está APROVADO
//        validarOrcamentoParaOS(request.idOrcamento());
//
//        // 4. Regra de Limitação de Entrada (Pátio x Agendamento)
//        validarCapacidadePatioEAgendamento(possuiAgendamento, carrosNoPatioAtual);
//    }
//
//    /**
//     * 1. Valida se o cliente (responsável que trouxe o veículo) existe
//     */
//    public void validarCliente(UUID idCliente) {
//        if (!clienteRepository.existsById(idCliente)) {
//            throw new EntidadeNaoEncontradaException("Cliente não encontrado com o ID: " + idCliente);
//        }
//    }
//
//    /**
//     * 2. Valida se o veículo existe a partir da Placa informada
//     */
//    public void validarVeiculoPorPlaca(String placa) {
//        if (placa == null || placa.isBlank()) {
//            throw new RegraDeNegocioException("A placa do veículo é obrigatória.");
//        }
//
//        boolean existe = veiculoRepository.existsByPlaca(placa.toUpperCase());
//        if (!existe) {
//            throw new EntidadeNaoEncontradaException("Veículo não encontrado com a placa: " + placa);
//        }
//    }
//
//    /**
//     * 3. Valida se o orçamento existe e se já foi APROVADO pelo cliente
//     */
//    public void validarOrcamentoParaOS(UUID idOrcamento) {
//        Orcamento orcamento = orcamentoRepository.findById(idOrcamento)
//                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento não encontrado com o ID: " + idOrcamento));
//
//        // Nenhuma OS pode ser iniciada se o orçamento não foi aprovado pelo cliente
//        if (!StatusOrcamento.APROVADO.equals(orcamento.getStatus())) {
//            throw new RegraDeNegocioException(
//                    "A Ordem de Serviço não pode ser iniciada. O Orçamento precisa estar APROVADO pelo cliente. Status atual: " + orcamento.getStatus()
//            );
//        }
//    }
//
//    /**
//     * 4. Valida a entrada no pátio: Se estiver lotado, só entra se tiver agendamento
//     */
//    private void validarCapacidadePatioEAgendamento(boolean possuiAgendamento, int carrosNoPatioAtual) {
//        if (!possuiAgendamento) {
//            if (carrosNoPatioAtual >= CAPACIDADE_MAXIMA_PATIO) {
//                throw new RegraDeNegocioException(
//                        "O pátio está com capacidade máxima (" + CAPACIDADE_MAXIMA_PATIO + " veículos) e a equipe sobrecarregada. " +
//                                "Não estamos recebendo veículos sem agendamento no momento."
//                );
//            }
//        }
//    }
//}