package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.OrcamentoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.inbound.orcamento.*;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import br.com.autoflow.ports.outbound.OrcamentoRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrcamentoUseCaseImpl implements
        CriarOrcamentoUseCase,
        AtualizarStatusOrcamentoUseCase,
        ListarOrcamentosUseCase,
        BuscarOrcamentoPorIdUseCase,
        DeletarOrcamentoUseCase {

    private final OrcamentoRepositoryPort orcamentoRepositoryPort;
    private final OrdemServicoRepositoryPort ordemServicoRepositoryPort;
    private final EstoqueRepositoryPort estoqueRepositoryPort;
    private final OrcamentoValidator orcamentoValidator;
    private final OrcamentoExpiradoUseCase orcamentoExpiradoUseCase;

    @Override
    @Transactional
    public Orcamento criar(UUID idOs, Orcamento orcamento) {
        OrdemServico ordemServico = ordemServicoRepositoryPort.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOs));

        orcamento.setOrdemServico(ordemServico);
        vincularServicosEItens(orcamento);

        orcamentoValidator.validarCriacao(idOs, orcamento);

        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataCriacao(LocalDateTime.now(ZoneId.systemDefault()));

        processarRegraTipoOrcamento(orcamento, ordemServico);

        return orcamentoRepositoryPort.save(orcamento);
    }

    @Override
    @Transactional
    public Orcamento atualizarStatus(UUID id, StatusOrcamento novoStatus) {
        Orcamento orcamento = buscarPorId(id);

        if (orcamento.getStatus() != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos PENDENTES podem ter o status alterado.");
        }
        orcamentoValidator.validarAtualizacaoStatus(novoStatus);

        if (orcamento.getDataExpiracao() != null && LocalDateTime.now(ZoneId.systemDefault()).isAfter(orcamento.getDataExpiracao())) {
            orcamento.expirar();
            orcamentoExpiradoUseCase.salvarOrcamentoExpirado(orcamento);
            throw new RegraNegocioException("Não foi possível alterar o status: Este orçamento está expirado.");
        }

        if (novoStatus == StatusOrcamento.APROVADO) {
            orcamentoValidator.validarEstoqueDisponivel(orcamento);
            deduzirItensDoEstoque(orcamento);
            orcamento.aprovar();
            orcamento = orcamentoRepositoryPort.save(orcamento);

            if (orcamento.getTipoOrcamento() != null &&
                    orcamento.getTipoOrcamento().name().equalsIgnoreCase("COMPLEMENTAR")) {
                OrdemServico ordemServico = orcamento.getOrdemServico();
                ordemServico.atualizarStatus(StatusOS.EM_EXECUCAO, "Orçamento complementar aprovado. Retomando execução.");
                ordemServico.carregarServicosDosOrcamentosAprovados();
                ordemServicoRepositoryPort.save(ordemServico);
            }
        } else {
            orcamento.aplicarNovoStatus(novoStatus);
            orcamento = orcamentoRepositoryPort.save(orcamento);
        }

        return orcamento;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> listarTodos() {
        return orcamentoRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Orcamento buscarPorId(UUID id) {
        return orcamentoRepositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> listarPorOrdemServico(UUID idOs) {
        List<Orcamento> orcamentos = orcamentoRepositoryPort.findByOrdemServicoIdOs(idOs);
        if (orcamentos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum orçamento encontrado para a Ordem de Serviço ID: ", idOs);
        }
        return orcamentos;
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        if (!orcamentoRepositoryPort.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Orçamento", id);
        }
        orcamentoRepositoryPort.deletarItensDiretosPorOrcamento(id);
        orcamentoRepositoryPort.deletarItensPorServicosDoOrcamento(id);
        orcamentoRepositoryPort.deletarServicosPorOrcamento(id);
    }

    private void deduzirItensDoEstoque(Orcamento orcamento) {
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().stream()
                    .filter(s -> s.getItens() != null)
                    .flatMap(s -> s.getItens().stream())
                    .forEach(item -> {
                        Estoque estoque = estoqueRepositoryPort.findById(item.getIdEstoque())
                                .orElseThrow(() -> new EntidadeNaoEncontradaException("Item de Estoque", item.getIdEstoque()));

                        estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - item.getQuantidade());
                        estoqueRepositoryPort.save(estoque);
                    });
        }
    }

    public List<String> verificarAvisosEstoque(Orcamento orcamento) {
        List<String> avisosEstoque = new ArrayList<>();
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().stream()
                    .filter(s -> s.getItens() != null)
                    .flatMap(s -> s.getItens().stream())
                    .forEach(item -> estoqueRepositoryPort.findById(item.getIdEstoque()).ifPresent(estoque -> {
                        if (estoque.deveDispararAlertaEstoqueBaixo()) {
                            avisosEstoque.add(String.format("ALERTA: O item '%s' atingiu nível crítico (%d restantes).",
                                    estoque.getNomeItem(), estoque.getQuantidadeEstoque()));
                        }
                    }));
        }
        return avisosEstoque;
    }

    private void vincularServicosEItens(Orcamento orcamento) {
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().forEach(servico -> {
                servico.setOrcamento(orcamento);
                if (servico.getItens() != null) {
                    servico.getItens().forEach(item -> {
                        item.setOrcamentoServico(servico);
                        item.setStatusReserva(StatusReservaEstoque.RESERVADO);
                    });
                }
            });
        }
    }

    private void processarRegraTipoOrcamento(Orcamento orcamento, OrdemServico ordemServico) {
        boolean ehComplementar = orcamento.getTipoOrcamento() != null &&
                orcamento.getTipoOrcamento().name().equalsIgnoreCase("COMPLEMENTAR");

        if (ehComplementar) {
            boolean temOrcamentoAprovado = ordemServico.getIdsOrcamento().stream()
                    .anyMatch(o -> o.getStatus() == StatusOrcamento.APROVADO);
            if (!temOrcamentoAprovado) {
                throw new RegraNegocioException("Não é possível criar um orçamento complementar sem que o orçamento inicial esteja aprovado.");
            }
            orcamento.setDataExpiracao(LocalDateTime.now(ZoneId.systemDefault()).plusHours(24));
            ordemServico.atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "OS pausada: Aguardando aprovação de orçamento complementar.");
            ordemServicoRepositoryPort.save(ordemServico);
        }
    }
}