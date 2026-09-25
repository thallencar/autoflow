package br.com.autoflow.application.validator;

import br.com.autoflow.domain.exception.DadosJaCadastradosException;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VeiculoValidator {

    private final VeiculoRepositoryPort veiculoRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final OrdemServicoRepositoryPort ordemServicoRepository;

    public void validarParaCriar(Veiculo veiculo) {
        String placaFormatada = formatarPlaca(veiculo.getPlaca());
        validarPlacaUnica(placaFormatada);
        validarClienteExiste(veiculo.getClienteId());
        validarAnoFabricacao(veiculo.getAnoFabricacao());
    }

    private void validarPlacaUnica(String placa) {
        if (veiculoRepository.existsByPlaca(placa)) {
            throw new DadosJaCadastradosException("Placa já cadastrada: " + placa);
        }
    }

    private void validarClienteExiste(UUID clienteId) {
        if (clienteId == null || !clienteRepository.existsById(clienteId)) {
            throw new EntidadeNaoEncontradaException("Cliente", clienteId);
        }
    }

    private void validarAnoFabricacao(Short ano) {
        if (ano == null) return;
        int anoAtual = Year.now(java.time.ZoneId.systemDefault()).getValue();
        if (ano > anoAtual + 1) {
            throw new RegraNegocioException("O ano de fabricação não pode ser maior que " + (anoAtual + 1));
        }
    }

    public String formatarPlaca(String placa) {
        if (placa == null) return null;
        return placa.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    public Veiculo buscarVeiculo(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Veículo : ", id));
    }

    public void validarParaAtualizar(UUID veiculoId, Veiculo veiculoParam) {
        if (veiculoParam.getPlaca() != null) {
            veiculoRepository.findByPlaca(veiculoParam.getPlaca()).ifPresent(veiculoEncontrado -> {
                if (!veiculoEncontrado.getId().equals(veiculoId)) {
                    throw new DadosJaCadastradosException("Placa já cadastrada: " + veiculoParam.getPlaca());
                }
            });
        }
        if (veiculoParam.getClienteId() != null) {
            validarClienteExiste(veiculoParam.getClienteId());
        }
        if (veiculoParam.getAnoFabricacao() != null) {
            validarAnoFabricacao(veiculoParam.getAnoFabricacao());
        }
    }

    public Veiculo validarParaDeletar(UUID id) {
        Veiculo veiculo = buscarVeiculo(id);
        if (ordemServicoRepository.existsByIdVeiculo(id)) {
            throw new RegraNegocioException("Não é possível excluir o veículo pois existem ordens de serviço vinculadas a ele.");
        }
        return veiculo;
    }
}