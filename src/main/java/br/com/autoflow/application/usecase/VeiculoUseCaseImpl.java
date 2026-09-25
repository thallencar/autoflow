package br.com.autoflow.application.usecase;

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
    private final VeiculoValidator veiculoValidator;

    @Override
    @Transactional
    public Veiculo criar(Veiculo veiculo) {
        veiculoValidator.validarParaCriar(veiculo);

        // Garante placa formatada e ID nulo para inserção
        veiculo.setPlaca(veiculoValidator.formatarPlaca(veiculo.getPlaca()));
        veiculo.setId(null);

        return veiculoRepository.save(veiculo);
    }

    @Override
    public List<Veiculo> listar() {
        return veiculoRepository.findAll();
    }

    @Override
    public Veiculo buscarPorId(UUID id) {
        return veiculoValidator.buscarVeiculo(id);
    }

    @Override
    @Transactional
    public Veiculo atualizar(UUID id, Veiculo veiculoParam) {
        Veiculo veiculoExistente = veiculoValidator.buscarVeiculo(id);

        if (veiculoParam.getPlaca() != null) {
            veiculoParam.setPlaca(veiculoValidator.formatarPlaca(veiculoParam.getPlaca()));
        }

        veiculoValidator.validarParaAtualizar(id, veiculoParam);

        veiculoExistente.atualizar(
                veiculoParam.getPlaca(),
                veiculoParam.getMarca(),
                veiculoParam.getModelo(),
                veiculoParam.getKmAtual(),
                veiculoParam.getAnoFabricacao(),
                veiculoParam.getCor()
        );

        return veiculoRepository.save(veiculoExistente);
    }

    @Override
    public Veiculo buscarPorPlaca(String placa) {
        String placaFormatada = veiculoValidator.formatarPlaca(placa);
        return veiculoRepository.findByPlaca(placaFormatada)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com a placa: " + placa));
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        Veiculo veiculo = veiculoValidator.validarParaDeletar(id);
        veiculoRepository.delete(veiculo);
    }
}