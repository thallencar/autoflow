package br.com.autoflow.application.service;

import br.com.autoflow.infrastructure.mapper.VeiculoMapper;
import org.springframework.stereotype.Service;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.application.dto.VeiculoResponse;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoMapper veiculoMapper;
    private final VeiculoValidator veiculoValidator;

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        veiculoValidator.validarParaCriar(request);
        Cliente cliente = veiculoValidator.buscarCliente(request.clienteId());
        Veiculo veiculo = veiculoMapper.toEntity(request, cliente);
        veiculo = veiculoRepository.save(veiculo);
        return veiculoMapper.toResponse(veiculo);
    }

    public List<VeiculoResponse> listar() {
        return veiculoRepository.findAll()
                .stream()
                .map(veiculoMapper::toResponse)
                .toList();
    }

    public VeiculoResponse buscarPorId(UUID id) {
        Veiculo veiculo = veiculoValidator.buscarVeiculo(id);
        return veiculoMapper.toResponse(veiculo);
    }

    @Transactional
    public VeiculoResponse atualizar(UUID id, VeiculoRequest request) {
        Veiculo veiculoExistente = veiculoValidator.buscarVeiculo(id);
        veiculoValidator.validarParaAtualizar(id, request);
        Cliente cliente = veiculoValidator.buscarCliente(request.clienteId());
        veiculoMapper.updateEntity(request, veiculoExistente, cliente);
        veiculoExistente = veiculoRepository.save(veiculoExistente);
        return veiculoMapper.toResponse(veiculoExistente);
    }

    @Transactional
    public void deletar(UUID id) {
        Veiculo veiculo = veiculoValidator.buscarVeiculo(id);
        veiculoRepository.delete(veiculo);
    }
}
