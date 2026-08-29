package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.FuncionarioRepository;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrdemServicoMapper;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
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
public class OrdemServicoService {

    private static final Logger log = LoggerFactory.getLogger(OrdemServicoService.class);

    private static final String NOME_ENTIDADE = "Ordem de Serviço";

    private final OrdemServicoRepository repository;
    private final OrdemServicoMapper mapper;
    private final OrdemServicoValidator validator;
    private final FuncionarioRepository funcionarioRepository;
    private final OrcamentoService orcamentoService;

    @Transactional(readOnly = true)
    public Page<OrdemServicoResponse> listarTodas(Pageable pageable) {
        Page<OrdemServico> lista = repository.findAll(pageable);
        return lista.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResponse> listarPorStatus(StatusOS status, Pageable pageable) {
        Page<OrdemServico> ordens = repository.findByStatusOS(status, pageable);
        return ordens.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        return mapper.toResponse(os);
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request, boolean possuiAgendamento) {
        List<StatusOS> statusIgnoradosNoPatio = List.of(StatusOS.ENTREGUE, StatusOS.CANCELADA);
        Long carrosNoPatio = repository.countByStatusOSNotIn(statusIgnoradosNoPatio);
        validator.validarCriacao(request, possuiAgendamento, carrosNoPatio);
        OrdemServico os = mapper.toEntity(request);
        ocuparMecanicoSeNecessario(request.idFuncionario());
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public OrdemServicoResponse atualizar(UUID id, OrdemServicoRequest request) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        validator.validarCliente(request.idCliente());
        validator.validarOrcamentosParaOS(request.idsOrcamento());
        validator.validarAlteracaoMecanico(request.idFuncionario(), id);

        gerenciarTrocaMecanico(os, request.idFuncionario());
        mapper.updateEntityFromRequest(os, request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public void atualizarStatusPagamento(UUID id, StatusPagamento novoStatus) {
        OrdemServico ordemServico = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        validator.validarAtualizacaoPagamento(ordemServico, novoStatus);
        ordemServico.setStPagamento(novoStatus);
        repository.save(ordemServico);
    }

    @Transactional
    public void deletar(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        if (os.getIdFuncionario() != null) {
            liberarMecanico(os.getIdFuncionario());
        }
        repository.deleteById(id);
    }

    @Transactional
    public OrdemServicoResponse atualizarStatus(UUID idOS, AtualizarStatusOSRequest request) {
        OrdemServico os = buscarOrdemServicoPorId(idOS);
        StatusOS novoStatus = request.status();

        validarRequisitosStatus(novoStatus, request.observacao(), os);
        processarEstoqueSeNecessario(os, novoStatus);
        os.atualizarStatus(novoStatus, request.observacao());

        liberarMecanicoSeFinalizada(os, novoStatus);

        return mapper.toResponse(repository.save(os));
    }

    @Transactional(readOnly = true)
    public MetricaOsResponse obterMetricasPorOS(UUID idOs) {
        OrdemServico ordemServico = repository.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, idOs));
        return mapper.toMetricaResponse(ordemServico);
    }

    @Transactional(readOnly = true)
    public Page<MetricaOsResponse> buscarMetricasComFiltro(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusOS status,
            Pageable pageable) {
        Page<OrdemServico> ordens = repository.findMetricasComFiltro(dataInicio, dataFim, status, pageable);
        return ordens.map(mapper::toMetricaResponse);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoVeiculoResponse> obterHistoricoPorVeiculo(UUID idVeiculo, Pageable pageable) {
        validator.validarVeiculoExiste(idVeiculo);
        Page<OrdemServico> ordens = repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo, pageable);
        if (ordens.isEmpty()) {
            throw new EntidadeNaoEncontradaException(NOME_ENTIDADE, idVeiculo);
        }
        return ordens.map(mapper::toHistoricoResponse);
    }

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

    private OrdemServico buscarOrdemServicoPorId(UUID idOS) {
        return repository.findById(idOS)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, idOS));
    }

    private void processarEstoqueSeNecessario(OrdemServico os, StatusOS novoStatus) {
        if (novoStatus != StatusOS.ORCAMENTO_APROVADO && novoStatus != StatusOS.EM_EXECUCAO) {
            return;
        }
        if (os.getIdsOrcamento() == null) return;

        for (Orcamento orcamento : os.getIdsOrcamento()) {
            if (orcamento.getStatus() == StatusOrcamento.PENDENTE) {
                orcamentoService.deduzirItensDoEstoque(orcamento);
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