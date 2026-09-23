package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.adapters.inbound.controller.dto.VeiculoResponse;
import br.com.autoflow.adapters.inbound.mapper.VeiculoMapper;

import br.com.autoflow.application.validator.VeiculoValidator;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.ports.inbound.veiculo.*;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeiculoUseCaseImpl implements
        CriarVeiculoUseCase,
        ListarVeiculosUseCase,
        BuscarVeiculoPorIdUseCase,
        AtualizarVeiculoUseCase,
        BuscarVeiculoPorPlacaUseCase,
        DeletarVeiculoUseCase {

    private final VeiculoRepositoryPort veiculoRepository;
    private final VeiculoMapper veiculoMapper;
    private final VeiculoValidator veiculoValidator;

    @Override
    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        veiculoValidator.validarParaCriar(request);
        Veiculo veiculo = veiculoMapper.toDomain(request);
        veiculo = veiculoRepository.save(veiculo);
        return veiculoMapper.toResponse(veiculo);
    }

    @Override
    public List<VeiculoResponse> listar() {
        return veiculoRepository.findAll()
                .stream()
                .map(veiculoMapper::toResponse)
                .toList();
    }

    @Override
    public VeiculoResponse buscarPorId(UUID id) {
        Veiculo veiculo = veiculoValidator.buscarVeiculo(id);
        return veiculoMapper.toResponse(veiculo);
    }

    @Override
    @Transactional
    public VeiculoResponse atualizar(UUID id, VeiculoRequest request) {
        Veiculo veiculoExistente = veiculoValidator.buscarVeiculo(id);
        veiculoValidator.validarParaAtualizar(id, request);

        veiculoExistente.atualizar(
                request.placa(),
                request.marca(),
                request.modelo(),
                request.kmAtual(),
                request.anoFabricacao(),
                request.cor()
        );

        veiculoExistente = veiculoRepository.save(veiculoExistente);
        return veiculoMapper.toResponse(veiculoExistente);
    }

    @Override
    @Transactional
    public VeiculoResponse buscarPorPlaca(String placa) {
        String placaFormatada = veiculoValidator.formatarPlaca(placa);
        Veiculo veiculo = veiculoRepository.findByPlaca(placaFormatada)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com a placa: " + placa));
        return veiculoMapper.toResponse(veiculo);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        Veiculo veiculo = veiculoValidator.validarParaDeletar(id);
        veiculoRepository.delete(veiculo);
    }
}