package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.OrcamentoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.*;
import br.com.autoflow.ports.inbound.orcamento.*;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
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
    private final FuncionarioRepositoryPort funcionarioRepository;

    @Override
    @Transactional
    public Orcamento criar(UUID idOs, Orcamento orcamento) {
        OrdemServico ordemServico = buscarOrdemServicoOuLancarExcecao(idOs);

        prepararNovoOrcamento(orcamento, ordemServico);
        validarCriacaoOrcamento(idOs, orcamento);

        Orcamento orcamentoSalvo = orcamentoRepositoryPort.save(orcamento);
        vincularOrcamentoNaOrdemServico(ordemServico, orcamentoSalvo);
        atualizarStatusOrdemServicoAposCriacao(ordemServico, orcamentoSalvo);

        return orcamentoSalvo;
    }

    @Override
    @Transactional
    public Orcamento atualizarStatus(UUID id, StatusOrcamento novoStatus) {
        Orcamento orcamento = buscarPorId(id);

        validarEstadoEValidadeOrcamento(orcamento, novoStatus);

        OrdemServico ordemServicoPersistida = buscarEAtualizarVinculoOrdemServico(orcamento);

        aplicarMudancaStatusOrcamento(orcamento, novoStatus, ordemServicoPersistida);

        salvarAlteracoesFinais(orcamento, ordemServicoPersistida);

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

    private OrdemServico buscarOrdemServicoOuLancarExcecao(UUID idOs) {
        return ordemServicoRepositoryPort.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOs));
    }

    private void prepararNovoOrcamento(Orcamento orcamento, OrdemServico ordemServico) {
        orcamento.setId(null);
        orcamento.setOrdemServico(ordemServico);

        vincularServicosEItens(orcamento);
        orcamento.recalcularTotais();

        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataCriacao(LocalDateTime.now(ZoneId.systemDefault()));

        processarRegraTipoOrcamento(orcamento, ordemServico);
    }

    private void validarCriacaoOrcamento(UUID idOs, Orcamento orcamento) {
        if (orcamentoValidator != null) {
            orcamentoValidator.validarCriacao(idOs, orcamento);
        }
    }

    private void vincularOrcamentoNaOrdemServico(OrdemServico ordemServico, Orcamento orcamentoSalvo) {
        if (ordemServico.getIdsOrcamento() == null) {
            ordemServico.setIdsOrcamento(new ArrayList<>());
        }
        ordemServico.getIdsOrcamento().add(orcamentoSalvo);
        ordemServicoRepositoryPort.save(ordemServico);
    }

    private void atualizarStatusOrdemServicoAposCriacao(OrdemServico ordemServico, Orcamento orcamento) {
        boolean ehComplementar = verificarSeEhComplementar(orcamento);

        if (ehComplementar) {
            ordemServico.atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "OS pausada: Aguardando aprovação de orçamento complementar.");
        } else {
            ordemServico.atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "Orçamento criado. Aguardando aprovação do cliente.");
        }

        ordemServicoRepositoryPort.save(ordemServico);
    }

    private boolean verificarSeEhComplementar(Orcamento orcamento) {
        return orcamento.getTipoOrcamento() != null &&
                orcamento.getTipoOrcamento().name().equalsIgnoreCase("COMPLEMENTAR");
    }

    private void validarEstadoEValidadeOrcamento(Orcamento orcamento, StatusOrcamento novoStatus) {
        if (orcamento.getStatus() != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos PENDENTES podem ter o status alterado.");
        }

        if (orcamentoValidator != null) {
            orcamentoValidator.validarAtualizacaoStatus(novoStatus);
        }

        if (orcamento.getDataExpiracao() != null && LocalDateTime.now(ZoneId.systemDefault()).isAfter(orcamento.getDataExpiracao())) {
            orcamento.expirar();
            orcamentoExpiradoUseCase.salvarOrcamentoExpirado(orcamento);
            throw new RegraNegocioException("Não foi possível alterar o status: Este orçamento está expirado.");
        }
    }

    private OrdemServico buscarEAtualizarVinculoOrdemServico(Orcamento orcamento) {
        OrdemServico ordemServicoDominio = orcamento.getOrdemServico();
        if (ordemServicoDominio == null || ordemServicoDominio.getIdOs() == null) {
            return null;
        }

        OrdemServico ordemServicoPersistida = ordemServicoRepositoryPort.findById(ordemServicoDominio.getIdOs())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", ordemServicoDominio.getIdOs()));

        if (ordemServicoPersistida.getIdsOrcamento() == null) {
            ordemServicoPersistida.setIdsOrcamento(new ArrayList<>());
        }

        boolean jaContem = ordemServicoPersistida.getIdsOrcamento().stream()
                .anyMatch(o -> o.getId().equals(orcamento.getId()));

        if (!jaContem) {
            ordemServicoPersistida.getIdsOrcamento().add(orcamento);
        }

        return ordemServicoPersistida;
    }

    private void aplicarMudancaStatusOrcamento(Orcamento orcamento, StatusOrcamento novoStatus, OrdemServico ordemServicoPersistida) {
        if (novoStatus == StatusOrcamento.APROVADO) {
            aprovarOrcamentoEAtualizarOrdemServico(orcamento, ordemServicoPersistida);
        } else {
            rejeitarOuExpirarOrcamento(orcamento, novoStatus, ordemServicoPersistida);
        }
    }

    private void aprovarOrcamentoEAtualizarOrdemServico(Orcamento orcamento, OrdemServico ordemServicoPersistida) {
        orcamentoValidator.validarEstoqueDisponivel(orcamento);
        deduzirItensDoEstoque(orcamento);
        orcamento.aprovar();

        if (ordemServicoPersistida != null) {
            boolean ehComplementar = verificarSeEhComplementar(orcamento);

            if (ehComplementar) {
                ordemServicoPersistida.atualizarStatus(StatusOS.EM_EXECUCAO, "Orçamento complementar aprovado. Retomando execução.");
            } else {
                ordemServicoPersistida.atualizarStatus(StatusOS.ORCAMENTO_APROVADO, "Orçamento aprovado pelo cliente via webhook/email. Iniciando execução.");
            }
            ordemServicoPersistida.carregarServicosDosOrcamentosAprovados();
        }
    }

    private void rejeitarOuExpirarOrcamento(Orcamento orcamento, StatusOrcamento novoStatus, OrdemServico ordemServicoPersistida) {
        orcamento.aplicarNovoStatus(novoStatus);

        boolean deveCancelarOS = (novoStatus == StatusOrcamento.RECUSADO || novoStatus == StatusOrcamento.EXPIRADO);
        if (deveCancelarOS && ordemServicoPersistida != null) {
            ordemServicoPersistida.atualizarStatus(StatusOS.CANCELADA, "Orçamento recusado pelo cliente via email/webhook.");
            if (ordemServicoPersistida.getIdFuncionario() != null) {
                funcionarioRepository.findById(ordemServicoPersistida.getIdFuncionario()).ifPresent(mecanico -> {
                    mecanico.liberar();
                    funcionarioRepository.save(mecanico);
                });
            }
        }
    }

    private void salvarAlteracoesFinais(Orcamento orcamento, OrdemServico ordemServicoPersistida) {
        if (ordemServicoPersistida != null) {
            ordemServicoRepositoryPort.save(ordemServicoPersistida);
        }
        orcamentoRepositoryPort.save(orcamento);
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
                servico.associarOrcamento(orcamento);
                if (servico.getItens() != null) {
                    servico.getItens().forEach(item -> {
                        item.setId(null);
                        item.associarOrcamentoServico(servico);
                        item.alterarStatusReserva(StatusReservaEstoque.RESERVADO);
                    });
                }
            });
        }
    }

    private void processarRegraTipoOrcamento(Orcamento orcamento, OrdemServico ordemServico) {
        boolean ehComplementar = verificarSeEhComplementar(orcamento);

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