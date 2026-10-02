package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.outbound.persistence.mapper.OrdemServicoEntityMapper;
import br.com.autoflow.application.validator.OrdemServicoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.inbound.ordemservico.*;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoUseCaseImpl implements AtualizarOrdemServicoUseCase,
        AtualizarStatusOrdemServicoUseCase,
        BuscarOrdemServicoPorIdUseCase,
        CriarOrdemServicoUseCase,
        DeletarOrdemServicoUseCase,
        ListarOrdemServicoUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrdemServicoUseCaseImpl.class);
    private static final String NOME_ENTIDADE = "Ordem de Serviço";

    private final OrdemServicoRepositoryPort repository;
    private final OrdemServicoValidator validator;
    private final FuncionarioRepositoryPort funcionarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrdemServico> listarOsAtivas(Pageable pageable) {
        return repository.findByStatusOSNotIn(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdemServico> listarTodas(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdemServico> listarPorStatus(StatusOS status, Pageable pageable) {
        return repository.findByStatusOS(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemServico buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
    }

    @Override
    @Transactional
    public OrdemServico criar(OrdemServico ordemServico, boolean possuiAgendamento) {
        List<StatusOS> statusIgnoradosNoPatio = List.of(StatusOS.ENTREGUE, StatusOS.CANCELADA);
        Long carrosNoPatio = repository.countByStatusOSNotIn(statusIgnoradosNoPatio);

        validator.validarCriacao(ordemServico, possuiAgendamento, carrosNoPatio);
        ocuparMecanicoSeNecessario(ordemServico.getIdFuncionario());

        return repository.save(ordemServico);
    }

    @Override
    @Transactional
    public OrdemServico atualizar(UUID id, OrdemServico ordemServicoAtualizada) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        validator.validarCliente(ordemServicoAtualizada.getIdCliente());

        List<UUID> idsOrcamento = ordemServicoAtualizada.getIdsOrcamento() != null
                ? ordemServicoAtualizada.getIdsOrcamento().stream().map(Orcamento::getId).toList()
                : List.of();

        validator.validarOrcamentosParaOS(idsOrcamento);
        validator.validarAlteracaoMecanico(ordemServicoAtualizada.getIdFuncionario(), id);

        gerenciarTrocaMecanico(os, ordemServicoAtualizada.getIdFuncionario());

        os.setIdFuncionario(ordemServicoAtualizada.getIdFuncionario());
        os.setIdsOrcamento(ordemServicoAtualizada.getIdsOrcamento());
        if (ordemServicoAtualizada.getDsRelatoCliente() != null) {
            os.setDsRelatoCliente(ordemServicoAtualizada.getDsRelatoCliente());
        }

        return repository.save(os);
    }

    @Override
    @Transactional
    public void atualizarStatusPagamento(UUID id, StatusPagamento novoStatus) {
        OrdemServico ordemServico = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        validator.validarAtualizacaoPagamento(ordemServico, novoStatus);
        ordemServico.setStPagamento(novoStatus);
        repository.save(ordemServico);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        if (os.getIdFuncionario() != null) {
            liberarMecanico(os.getIdFuncionario());
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public OrdemServico atualizarStatus(UUID idOS, StatusOS novoStatus, String observacao) {
        OrdemServico os = buscarPorId(idOS);

        validarRequisitosStatus(novoStatus, observacao, os);
        processarEstoqueSeNecessario(os, novoStatus);
        os.atualizarStatus(novoStatus, observacao);

        liberarMecanicoSeFinalizada(os, novoStatus);

        return repository.save(os);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemServico obterMetricasPorOS(UUID idOs) {
        return repository.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, idOs));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdemServico> buscarMetricasComFiltro(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusOS status,
            Pageable pageable) {
        return repository.findMetricasComFiltro(dataInicio, dataFim, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdemServico> obterHistoricoPorVeiculo(UUID idVeiculo, Pageable pageable) {
        validator.validarVeiculoExiste(idVeiculo);
        Page<OrdemServico> ordens = repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo, pageable);
        if (ordens.isEmpty()) {
            throw new EntidadeNaoEncontradaException(NOME_ENTIDADE, idVeiculo);
        }
        return ordens;
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processarCancelamentosAutomaticos() {
        List<OrdemServico> ordensPendentes = repository.findByStatusOS(StatusOS.AGUARDANDO_APROVACAO, Pageable.unpaged()).getContent();

        for (OrdemServico os : ordensPendentes) {
            StatusOS statusAntigo = os.getStatusOS();
            os.verificarCancelamentoAutomatico(3, BigDecimal.valueOf(30.00));
            if (statusAntigo != os.getStatusOS()) {
                if (os.getIdFuncionario() != null) {
                    liberarMecanico(os.getIdFuncionario());
                }
                repository.save(os);
                log.info("ALERTA AGENDADO: A OS ID {} foi cancelada automaticamente por falta de aprovação.", os.getIdOs());
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void processarAbandonoTecnico() {
        List<OrdemServico> ordensPendentes = repository.findByStatusOS(StatusOS.AGUARDANDO_APROVACAO, Pageable.unpaged()).getContent();

        for (OrdemServico os : ordensPendentes) {
            StatusOS statusAntigo = os.getStatusOS();
            os.verificarAbandonoTecnico(60);

            if (statusAntigo != os.getStatusOS()) {
                if (os.getIdFuncionario() != null) {
                    liberarMecanico(os.getIdFuncionario());
                }
                repository.save(os);
                log.info("ALERTA AGENDADO: A OS ID {} foi marcada como abandonada tecnicamente.", os.getIdOs());
            }
        }
    }

    private void processarEstoqueSeNecessario(OrdemServico os, StatusOS novoStatus) {
        if (novoStatus != StatusOS.ORCAMENTO_APROVADO && novoStatus != StatusOS.EM_EXECUCAO) {
            return;
        }
        if (os.getIdsOrcamento() == null) return;

        for (Orcamento orcamento : os.getIdsOrcamento()) {
            if (orcamento.getStatus() == StatusOrcamento.PENDENTE) {
                // lógica de estoque se necessário
            }
        }
    }

    private void ocuparMecanicoSeNecessario(UUID idFuncionario) {
        if (idFuncionario != null) {
            Funcionario mecanico = funcionarioRepository.findById(idFuncionario)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, idFuncionario));
            mecanico.ocupar();
            funcionarioRepository.save(mecanico);
        }
    }

    private void liberarMecanico(UUID idFuncionario) {
        funcionarioRepository.findById(idFuncionario).ifPresent(mecanico -> {
            mecanico.liberar();
            funcionarioRepository.save(mecanico);
        });
    }

    private void gerenciarTrocaMecanico(OrdemServico osAtual, UUID novoIdFuncionario) {
        UUID antigoIdFuncionario = osAtual.getIdFuncionario();

        if (antigoIdFuncionario != null && !antigoIdFuncionario.equals(novoIdFuncionario)) {
            liberarMecanico(antigoIdFuncionario);
        }

        if (novoIdFuncionario != null && !novoIdFuncionario.equals(antigoIdFuncionario)) {
            ocuparMecanicoSeNecessario(novoIdFuncionario);
        }
    }

    private void validarRequisitosStatus(StatusOS novoStatus, String observacao, OrdemServico os) {
        if ((novoStatus == StatusOS.EM_DIAGNOSTICO || novoStatus == StatusOS.AGUARDANDO_APROVACAO) && os.getIdFuncionario() == null) {
            throw new RegraNegocioException("Não é possível iniciar o diagnóstico sem um mecânico/funcionário alocado na Ordem de Serviço.");
        }
        if (novoStatus == StatusOS.AGUARDANDO_APROVACAO) {
            validator.validarDiagnosticoPreenchido(observacao);
        }
    }

    private void liberarMecanicoSeFinalizada(OrdemServico os, StatusOS novoStatus) {
        boolean statusFinalizado = novoStatus == StatusOS.FINALIZADA
                || novoStatus == StatusOS.ENTREGUE
                || novoStatus == StatusOS.CANCELADA;

        if (statusFinalizado && os.getIdFuncionario() != null) {
            funcionarioRepository.findById(os.getIdFuncionario()).ifPresent(mecanico -> {
                mecanico.liberar();
                funcionarioRepository.save(mecanico);
            });
        }
    }
}