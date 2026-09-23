package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.*;
import br.com.autoflow.adapters.inbound.mapper.EstoqueMapper;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueUseCase {

    private final EstoqueRepositoryPort repositoryPort;
    private final EstoqueMapper estoqueMapper;
    private static final String NOME_ENTIDADE = "Item de Estoque";

    @Transactional
    public EstoqueResponse criar(EstoqueRequest request) {
        Estoque estoque = estoqueMapper.toDomain(request);
        estoque = repositoryPort.save(estoque);
        estoque.deveDispararAlertaEstoqueBaixo();

        return estoqueMapper.toResponse(estoque);
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listarTodos() {
        return repositoryPort.findAll().stream()
                .map(estoqueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstoqueResponse buscarPorId(UUID id) {
        return estoqueMapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public EstoqueResponse adicionarQuantidade(UUID id, AdicionarEstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.adicionarQuantidade(request.quantidade());
        estoque.deveDispararAlertaEstoqueBaixo();
        return estoqueMapper.toResponse(repositoryPort.save(estoque));
    }

    @Transactional
    public EstoqueResponse atualizarValorUnitario(UUID id, AtualizarValorEstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.atualizarValorUnitario(request.valorUnitario());
        return estoqueMapper.toResponse(repositoryPort.save(estoque));
    }

    @Transactional
    public EstoqueResponse atualizar(UUID id, EstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.atualizarDados(
                request.nomeItem(),
                request.nomeMarca(),
                request.valorUnitario(),
                request.quantidadeEstoque(),
                request.quantidadeMinima(),
                request.tipoCategoria()
        );
        estoque.deveDispararAlertaEstoqueBaixo();
        return estoqueMapper.toResponse(repositoryPort.save(estoque));
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listarInsumosComEstoqueBaixo() {
        return repositoryPort.findAll().stream()
                .filter(Estoque::deveDispararAlertaEstoqueBaixo)
                .map(estoqueMapper::toResponse)
                .toList();
    }

    @Transactional
    public void reservarEstoqueParaItens(List<OrcamentoItemRequest> itensRequest) {
        if (itensRequest == null) return;

        for (OrcamentoItemRequest itemDto : itensRequest) {
            Estoque estoque = buscarEntidadePorId(itemDto.idEstoque());

            if (estoque.getQuantidadeEstoque() < itemDto.quantidade()) {
                throw new IllegalStateException("Estoque insuficiente para o item: " + estoque.getNomeItem());
            }

            estoque.removerQuantidade(itemDto.quantidade());
            repositoryPort.save(estoque);
        }
    }

    @Transactional
    public void devolverEstoqueDeItens(List<OrcamentoItemRequest> itensRequest) {
        if (itensRequest == null) return;

        for (OrcamentoItemRequest itemDto : itensRequest) {
            Estoque estoque = buscarEntidadePorId(itemDto.idEstoque());
            estoque.adicionarQuantidade(itemDto.quantidade());
            repositoryPort.save(estoque);
        }
    }

    private Estoque buscarEntidadePorId(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
    }
}