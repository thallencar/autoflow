package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.*;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.repository.EstoqueRepository;
import br.com.autoflow.infrastructure.mapper.EstoqueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final EstoqueMapper estoqueMapper;

    @Transactional
    public EstoqueResponse criar(EstoqueRequest request) {
        Estoque estoque = estoqueMapper.toEntity(request);
        estoque = estoqueRepository.save(estoque);
        estoque.deveDispararAlertaEstoqueBaixo();

        return estoqueMapper.toResponse(estoque);
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listarTodos() {
        return estoqueRepository.findAll().stream()
                .map(estoqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstoqueResponse buscarPorId(UUID id) {
        return estoqueMapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public EstoqueResponse adicionarQuantidade(UUID id, AdicionarEstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        int quantidadeAtual = estoque.getQuantidadeEstoque() != null ? estoque.getQuantidadeEstoque() : 0;
        estoque.setQuantidadeEstoque(quantidadeAtual + request.quantidade());
        estoque.deveDispararAlertaEstoqueBaixo();
        return estoqueMapper.toResponse(estoqueRepository.save(estoque));
    }

    @Transactional
    public EstoqueResponse atualizarValorUnitario(UUID id, AtualizarValorEstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.setValorUnitario(request.valorUnitario());
        return estoqueMapper.toResponse(estoqueRepository.save(estoque));
    }

    @Transactional
    public EstoqueResponse atualizar(UUID id, EstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.setNomeItem(request.nomeItem());
        estoque.setNomeMarca(request.nomeMarca());
        estoque.setValorUnitario(request.valorUnitario());
        estoque.setQuantidadeEstoque(request.quantidadeEstoque());
        estoque.setQuantidadeMinima(request.quantidadeMinima());
        estoque.setTipoCategoria(request.tipoCategoria());
       estoque.deveDispararAlertaEstoqueBaixo();
        return estoqueMapper.toResponse(estoqueRepository.save(estoque));
    }

    private Estoque buscarEntidadePorId(UUID id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listarInsumosComEstoqueBaixo() {
        return estoqueRepository.findAll().stream()
                .filter(Estoque::deveDispararAlertaEstoqueBaixo)
                .map(estoqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reservarEstoqueParaItens(List<OrcamentoItemRequest> itensRequest) {
        if (itensRequest == null) return;

        for (OrcamentoItemRequest itemDto : itensRequest) {
            Estoque estoque = buscarEntidadePorId(itemDto.idEstoque());

            if (estoque.getQuantidadeEstoque() < itemDto.quantidade()) {
                throw new IllegalStateException("Estoque insuficiente para o item: " + estoque.getNomeItem());
            }

            estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - itemDto.quantidade());
            estoqueRepository.save(estoque);
        }

    }
    /**
     * Regra de Negócio: Devolve os itens ao estoque caso o orçamento seja rejeitado.
     */
    @Transactional
    public void devolverEstoqueDeItens(List<OrcamentoItemRequest> itensRequest) {
        if (itensRequest == null) return;

        for (OrcamentoItemRequest itemDto : itensRequest) {
            Estoque estoque = buscarEntidadePorId(itemDto.idEstoque());

            estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() + itemDto.quantidade());
            estoqueRepository.save(estoque);
        }
    }

}