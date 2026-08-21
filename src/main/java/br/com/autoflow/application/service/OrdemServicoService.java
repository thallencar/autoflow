package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.repository.FuncionarioRepository;
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
    private final OrdemServicoRepository repository;
    private final OrdemServicoMapper mapper;
    private final OrdemServicoValidator validator;
    private final FuncionarioRepository funcionarioRepository;
    private final OrcamentoService orcamentoService;

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarTodas() {
        List<OrdemServico> lista = repository.findAll();
        return mapper.toResponseList(lista);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(UUID id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));
        return mapper.toResponse(os);
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request, boolean possuiAgendamento) {
        Long carrosNoPatio = repository.countByStatusOSNot(StatusOS.ENTREGUE);
        validator.validarCriacao(request, possuiAgendamento, carrosNoPatio);
        OrdemServico os = mapper.toEntity(request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public OrdemServicoResponse atualizar(UUID id, OrdemServicoRequest request) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));

        validator.validarCliente(request.idCliente());
        validator.validarOrcamentosParaOS(request.idsOrcamento());
        mapper.updateEntityFromRequest(os, request);
        return mapper.toResponse(repository.save(os));
    }

    @Transactional
    public void atualizarStatusPagamento(UUID id, StatusPagamento novoStatus) {
        OrdemServico ordemServico = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", id));
        validator.validarAtualizacaoPagamento(ordemServico, novoStatus);
        ordemServico.setStPagamento(novoStatus);
        repository.save(ordemServico);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Ordem de Serviço", id);
        }
        repository.deleteById(id);
    }
    @Transactional
    public OrdemServicoResponse atualizarStatus(UUID idOS, AtualizarStatusOSRequest request) {
        OrdemServico os = repository.findById(idOS)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOS));

        StatusOS novoStatus = request.status();
        if (novoStatus == StatusOS.ORCAMENTO_APROVADO || novoStatus == StatusOS.EM_EXECUCAO) {
            if (os.getIdsOrcamento() != null) {
                for (Orcamento orcamento : os.getIdsOrcamento()) {
                    if (orcamento.getStatus() == StatusOrcamento.PENDENTE) {
                        orcamentoService.deduzirItensDoEstoque(orcamento);
                    }
                }
            }
        }
        if (novoStatus == StatusOS.EM_DIAGNOSTICO) {
            validator.validarAlocacaoMecanico(null, os.getIdFuncionario());
            Funcionario mecanico = funcionarioRepository.findById(os.getIdFuncionario())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário", os.getIdFuncionario()));
            mecanico.ocupar();
            funcionarioRepository.save(mecanico);
        }
        os.atualizarStatus(novoStatus, request.observacao());

        if (novoStatus == StatusOS.FINALIZADA || novoStatus == StatusOS.ENTREGUE || novoStatus == StatusOS.CANCELADA) {
            if (os.getIdFuncionario() != null) {
                funcionarioRepository.findById(os.getIdFuncionario()).ifPresent(mecanico -> {
                    mecanico.liberar();
                    funcionarioRepository.save(mecanico);
                });
            }
        }
        OrdemServico osSalva = repository.save(os);
        return mapper.toResponse(osSalva);
    }

    @Transactional(readOnly = true)
    public MetricaOsResponse obterMetricasPorOS(UUID idOs) {
        OrdemServico ordemServico = repository.findById(idOs)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", idOs));
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
    public List<HistoricoVeiculoResponse> obterHistoricoPorVeiculo(UUID idVeiculo) {
        validator.validarVeiculoExiste(idVeiculo);
        List<OrdemServico> ordens = repository.findByIdVeiculoOrderByDtAberturaOsDesc(idVeiculo);
        if (ordens.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Veículo : ", idVeiculo);
        }
        return ordens.stream()
                .map(mapper::toHistoricoResponse)
                .toList();
    }

    @Scheduled(cron = "0 0 8 * * *")// Roda todo dia as 08:00 da manhã
    @Transactional
    public void processarCancelamentosAutomaticos() {
        List<OrdemServico> ordensPendentes = repository.findByStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        for (OrdemServico os : ordensPendentes) {
            // Regra: Prazo limite de 3 dias e taxa de R$ 30,00 por dia excedido (Art. 40 CDC)
            StatusOS statusAntigo = os.getStatusOS();
            os.verificarCancelamentoAutomatico(3, BigDecimal.valueOf(30.00));
            if (statusAntigo != os.getStatusOS()) {
                repository.save(os);
                log.info("ALERTA AGENDADO: A OS ID {} foi cancelada automaticamente por falta de aprovação.", os.getIdOs());
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *") // Roda todo dia às 09:00
    @Transactional
    public void processarAbandonoTecnico() {
        // Busca OS aguardando aprovação para verificar abandono (ex: 60 dias)
        List<OrdemServico> ordensPendentes = repository.findByStatusOS(StatusOS.AGUARDANDO_APROVACAO);

        for (OrdemServico os : ordensPendentes) {
            StatusOS statusAntigo = os.getStatusOS();
            os.verificarAbandonoTecnico(60);

            if (statusAntigo != os.getStatusOS()) {
                repository.save(os);
                log.warn("ALERTA AGENDADO: A OS ID {} foi marcada como abandonada tecnicamente.", os.getIdOs());
            }
        }
    }
}