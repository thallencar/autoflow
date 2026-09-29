package br.com.autoflow.ports.inbound.ordemservico;

import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.model.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ListarOrdemServicoUseCase {
    Page<OrdemServico> listarTodas(Pageable pageable);
    Page<OrdemServico> listarPorStatus(StatusOS status, Pageable pageable);
    OrdemServico obterMetricasPorOS(UUID idOs);
    Page<OrdemServico> buscarMetricasComFiltro(LocalDateTime inicio, LocalDateTime fim, StatusOS status, Pageable pageable);
    Page<OrdemServico> obterHistoricoPorVeiculo(UUID idVeiculo, Pageable pageable);
    }
