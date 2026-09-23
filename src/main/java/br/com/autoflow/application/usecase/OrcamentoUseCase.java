package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoResponse;
import br.com.autoflow.application.validator.OrcamentoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusReservaEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.outbound.*;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.adapters.inbound.mapper.OrcamentoMapper;
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
public class OrcamentoUseCase {

    private final OrcamentoRepositoryPort orcamentoRepositoryPort;
    private final OrcamentoMapper orcamentoMapper;
    private final OrcamentoValidator orcamentoValidator;
    private final OrdemServicoRepositoryPort ordemServicoRepositoryPort;
    private final OrcamentoExpiradoUseCase orcamentoExpiradoUseCase;
    private final EstoqueRepositoryPort estoqueRepositoryPort;

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        if (orcamentoValidator != null) {
            orcamentoValidator.validarCriacao(request);
        }
        Orcamento orcamento = orcamentoMapper.toEntity(request);
        OrdemServico ordemServico = ordemServicoRepositoryPort.findById(request.idOs())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Ordem de Serviço", request.idOs()));
        orcamento.setOrdemServico(ordemServico);

        vincularServicosEItens(orcamento);

        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setDataCriacao(LocalDateTime.now(ZoneId.systemDefault()));

        processarRegraTipoOrcamento(orcamento, ordemServico, request);

        orcamento = orcamentoRepositoryPort.save(orcamento);
        return mapToResponseComAvisos(orcamento);
    }

    @Transactional
    public OrcamentoResponse atualizarStatus(UUID id, AtualizarStatusOrcamentoRequest request) {
        Orcamento orcamento = orcamentoRepositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", id));

        if (orcamento.getStatus() != StatusOrcamento.PENDENTE) {
            throw new RegraNegocioException("Apenas orçamentos PENDENTES podem ter o status alterado.");
        }
        if (orcamentoValidator != null) {
            orcamentoValidator.validarAtualizacaoStatus(request.status());
        }

        if (orcamento.getDataExpiracao() != null && LocalDateTime.now(ZoneId.systemDefault()).isAfter(orcamento.getDataExpiracao())) {
            orcamento.expirar();
            orcamentoExpiradoUseCase.salvarOrcamentoExpirado(orcamento);
            throw new RegraNegocioException("Não foi possível alterar o status: Este orçamento está expirado.");
        }
        if (request.status() == StatusOrcamento.APROVADO) {
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
            orcamento.aplicarNovoStatus(request.status());
            orcamento = orcamentoRepositoryPort.save(orcamento);
        }
        return mapToResponseComAvisos(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarTodos() {
        return orcamentoRepositoryPort.findAll().stream()
                .map(this::mapToResponseComAvisos)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(UUID id) {
        Orcamento orcamento = orcamentoRepositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento ", id));
        return mapToResponseComAvisos(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarPorOrdemServico(UUID idOs) {
        List<Orcamento> orcamientos = orcamentoRepositoryPort.findByOrdemServicoIdOs(idOs);
        if (orcamientos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum orçamento encontrado para a Ordem de Serviço ID: ", idOs);
        }
        return orcamientos.stream()
                .map(this::mapToResponseComAvisos)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!orcamentoRepositoryPort.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Orçamento", id);
        }
        orcamentoRepositoryPort.deletarItensDiretosPorOrcamento(id);
        orcamentoRepositoryPort.deletarItensPorServicosDoOrcamento(id);
        orcamentoRepositoryPort.deletarServicosPorOrcamento(id);
    }

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
                        Estoque estoque = estoqueRepositoryPort.findById(item.getIdEstoque())
                                .orElseThrow(() -> new EntidadeNaoEncontradaException("Item de Estoque", item.getIdEstoque()));

                        if (estoque.getQuantidadeEstoque() < item.getQuantidade()) {
                            throw new RegraNegocioException(
                                    String.format("Saldo insuficiente para a peça %s no momento da aprovação.", estoque.getNomeItem())
                            );
                        }
                        estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - item.getQuantidade());
                        estoqueRepositoryPort.save(estoque);

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
                    .forEach(item -> estoqueRepositoryPort.findById(item.getIdEstoque()).ifPresent(estoque -> {
                        if (estoque.deveDispararAlertaEstoqueBaixo()) {
                            avisosEstoque.add(String.format("ALERTA: O item '%s' atingiu nível crítico (%d restantes).",
                                    estoque.getNomeItem(), estoque.getQuantidadeEstoque()));
                        }
                    }));
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

    private void vincularServicosEItens(Orcamento orcamento) {
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
    }

    private void processarRegraTipoOrcamento(Orcamento orcamento, OrdemServico ordemServico, OrcamentoRequest request) {
        boolean ehComplementar = request.tipoOrcamento() != null &&
                request.tipoOrcamento().name().equalsIgnoreCase("COMPLEMENTAR");

        if (ehComplementar) {
            boolean temOrcamentoAprovado = ordemServico.getIdsOrcamento().stream()
                    .anyMatch(o -> o.getStatus() == StatusOrcamento.APROVADO);
            if (!temOrcamentoAprovado) {
                throw new RegraNegocioException("Não é possível criar um orçamento complementar sem que o orçamento inicial esteja aprovado.");
            }
            orcamento.setDataExpiracao(LocalDateTime.now(ZoneId.systemDefault()).plusHours(24));
            ordemServico.atualizarStatus(StatusOS.AGUARDANDO_APROVACAO, "OS pausada: Aguardando aprovação de orçamento complementar.");
            ordemServicoRepositoryPort.save(ordemServico);
        } else {
            if (orcamento.getDataExpiracao() == null) {
                orcamento.setDataExpiracao(request.dataExpiracao());
            }
        }
    }
}