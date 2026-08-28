package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.OrdemServicoRepository;
import br.com.autoflow.domain.repository.VeiculoRepository;
import br.com.autoflow.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VeiculoValidator {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public void validarParaCriar(VeiculoRequest request) {
        String placaFormatada = formatarPlaca(request.placa());
        validarPlacaUnica(placaFormatada);
        validarClienteExiste(request.clienteId());
        validarAnoFabricacao(request.anoFabricacao());
    }

    private void validarPlacaUnica(String placa) {
        if (veiculoRepository.existsByPlaca(placa)) {
            throw new DadosJaCadastradosException("Placa já cadastrada: " + placa);
        }
    }

    private void validarClienteExiste(UUID clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntidadeNaoEncontradaException("Cliente", clienteId);
        }
    }

    private void validarAnoFabricacao(Short ano) {
        int anoAtual = Year.now().getValue();
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

    public Cliente buscarCliente(UUID clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado com o ID: ", clienteId));
    }

    public void validarParaAtualizar(UUID veiculoId, VeiculoRequest request) {
        String placaFormatada = formatarPlaca(request.placa());
        if (placaFormatada != null) {
            veiculoRepository.findByPlaca(placaFormatada).ifPresent(veiculoEncontrado -> {
                if (!veiculoEncontrado.getId().equals(veiculoId)) {
                    throw new DadosJaCadastradosException("Placa já cadastrada: " + placaFormatada);
                }
            });
        }
        if (request.clienteId() != null) {
            validarClienteExiste(request.clienteId());
        }
    }
    public Veiculo validarParaDeletar(UUID id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Veículo: ", id));
        if (ordemServicoRepository.existsByIdVeiculo(id)) {
            throw new RegraNegocioException("Não é possível excluir o veículo pois existem ordens de serviço vinculadas a ele.");
        }
        return veiculo;
    }
}
