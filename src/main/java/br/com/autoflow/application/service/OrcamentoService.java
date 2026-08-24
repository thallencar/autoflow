package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.*;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.OrcamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoMapper orcamentoMapper;
    private final OrcamentoValidator orcamentoValidator;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoExpiradoService orcamentoExpiradoService;
    private final EstoqueRepository estoqueRepository;

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        if (orcamentoValidator != null) {
            orcamentoValidator.validarCriacao(request);
        }

        Orcamento orcamento = orcamentoMapper.toEntity(request);
        OrdemServico ordemServico = ordemServicoRepository.findById(request.idOs())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", request.idOs()));
        orcamento.setOrdemServico(ordemServico);

        if (orcamento.getServicos() != null) {
            for (var servico : orcamento.getServicos()) {
                servico.setOrcamento(orcamento);
                if (servico.getItens() != null) {
                    for (var item : servico.getItens()) {
                        item.setOrcamentoServico(servico);
                        item.setStatusReserva(StatusReservaEstoque.RESERVADO);
                    }
                }
            }
        }
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataCriacao(LocalDateTime.now());

        boolean ehComplementar = request.tipoOrcamento() != null &&
                request.tipoOrcamento().name().equalsIgnoreCase("COMPLEMENTAR");

        if (ehComplementar) {
            // Regra: Valida se já existe algum orçamento aprovado na OS antes de permitir o complementar
            boolean temOrcamentoAprovado = ordemServico.getIdsOrcamento().stream()
                    .anyMatch(o -> o.getStatus() == StatusOrcamento.APROVADO);
            if (!temOrcamentoAprovado) {
                throw new RegraNegocioException("Não é possível criar um orçamento complementar sem que o orçamento inicial esteja aprovado.");
            }
            orcamento.setDataExpiracao(LocalDateTime.now().plusHours(24));
            ordemServico.atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "OS pausada: Aguardando aprovação de orçamento complementar.");
            ordemServicoRepository.save(ordemServico);
        } else {
            if (orcamento.getDataExpiracao() == null) {
                orcamento.setDataExpiracao(request.dataExpiracao());
            }
        }

        orcamento = orcamentoRepository.save(orcamento);
        return mapToResponseComAvisos(orcamento);
    }

    @Transactional
    public OrcamentoResponse atualizarStatus(UUID id, AtualizarStatusOrcamentoRequest request) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", id));

        if (orcamento.getStatus() != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos PENDENTES podem ter o status alterado.");
        }
        if (orcamentoValidator != null) {
            orcamentoValidator.validarAtualizacaoStatus(request.status());
        }

        if (orcamento.getDataExpiracao() != null && LocalDateTime.now().isAfter(orcamento.getDataExpiracao())) {
            orcamento.expirar();
            orcamentoExpiradoService.salvarOrcamentoExpirado(orcamento);
            throw new RegraNegocioException("Não foi possível alterar o status: Este orçamento está expirado.");
        }
        if (request.status() == StatusOrcamento.APROVADO) {
            deduzirItensDoEstoque(orcamento);
            orcamento.aprovar();
            orcamento = orcamentoRepository.save(orcamento);
            if (orcamento.getTipoOrcamento() != null &&
                    orcamento.getTipoOrcamento().name().equalsIgnoreCase("COMPLEMENTAR")) {
                OrdemServico ordemServico = orcamento.getOrdemServico();
                ordemServico.atualizarStatus(StatusOS.EM_EXECUCAO, "Orçamento complementar aprovado. Retomando execução.");
                ordemServico.carregarServicosDosOrcamentosAprovados();
                ordemServicoRepository.save(ordemServico);
            }
        } else {
            orcamento.aplicarNovoStatus(request.status());
            orcamento = orcamentoRepository.save(orcamento);
        }
        return mapToResponseComAvisos(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarTodos() {
        return orcamentoRepository.findAll().stream()
                .map(this::mapToResponseComAvisos)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(UUID id) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
        return mapToResponseComAvisos(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarPorOrdemServico(UUID idOs) {
        List<Orcamento> orcamientos = orcamentoRepository.findByOrdemServicoIdOs(idOs);
        if (orcamientos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum orçamento encontrado para a Ordem de Serviço ID: ", idOs);
        }
        return orcamientos.stream()
                .map(this::mapToResponseComAvisos)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!orcamentoRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Orçamento", id);
        }
        orcamentoRepository.deletarItensDiretosPorOrcamento(id);
        orcamentoRepository.deletarItensPorServicosDoOrcamento(id);
        orcamentoRepository.deletarServicosPorOrcamento(id);
    }

    @Transactional
    public List<String> deduzirItensDoEstoque(Orcamento orcamento) {
        List<String> avisosEstoque = new ArrayList<>();

        if (orcamentoValidator != null) {
            orcamentoValidator.validarEstoqueDisponivel(orcamento);
        }
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().stream()
                    .filter(servico -> servico.getItens() != null)
                    .flatMap(servico -> servico.getItens().stream())
                    .forEach(item -> {
                        Estoque estoque = estoqueRepository.findById(item.getIdEstoque())
                                .orElseThrow(() -> new EntidadeNaoEncontradaException("Item de Estoque", item.getIdEstoque()));

                        if (estoque.getQuantidadeEstoque() < item.getQuantidade()) {
                            throw new RegraNegocioException(
                                    String.format("Saldo insuficiente para a peça %s no momento da aprovação.", estoque.getNomeItem())
                            );
                        }
                        estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - item.getQuantidade());
                        estoqueRepository.save(estoque);

                        if (estoque.deveDispararAlertaEstoqueBaixo()) {
                            avisosEstoque.add(String.format("ALERTA: O item '%s' atingiu nível crítico (%d restantes).",
                                    estoque.getNomeItem(), estoque.getQuantidadeEstoque()));
                        }
                    });
        }
        return avisosEstoque;
    }

    private List<String> verificarAvisosEstoque(Orcamento orcamento) {
        List<String> avisosEstoque = new ArrayList<>();
        if (orcamento.getServicos() != null) {
            orcamento.getServicos().stream()
                    .filter(servico -> servico.getItens() != null)
                    .flatMap(servico -> servico.getItens().stream())
                    .forEach(item -> {
                        estoqueRepository.findById(item.getIdEstoque()).ifPresent(estoque -> {
                            if (estoque.deveDispararAlertaEstoqueBaixo()) {
                                avisosEstoque.add(String.format("ALERTA: O item '%s' atingiu nível crítico (%d restantes).",
                                        estoque.getNomeItem(), estoque.getQuantidadeEstoque()));
                            }
                        });
                    });
        }
        return avisosEstoque;
    }

    private OrcamentoResponse mapToResponseComAvisos(Orcamento orcamento) {
        OrcamentoResponse response = orcamentoMapper.toResponse(orcamento);
        List<String> avisos = verificarAvisosEstoque(orcamento);

        return new OrcamentoResponse(
                response.id(),
                response.idOs(),
                response.tipoOrcamento(),
                response.status(),
                response.dataCriacao(),
                response.dataExpiracao(),
                response.dataDecisao(),
                response.subtotalPecas(),
                response.maoObra(),
                response.total(),
                response.servicos(),
                avisos
        );
    }
}