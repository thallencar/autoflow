package com.autoflow.application.service;

import com.autoflow.application.dto.EstoqueRequest;
import com.autoflow.application.dto.EstoqueResponse;
import com.autoflow.domain.model.Estoque;
import com.autoflow.domain.repository.EstoqueRepository;
import com.autoflow.infrastructure.mapper.EstoqueMapper;
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
        Estoque estoque = estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));
        return estoqueMapper.toResponse(estoque);
    }
    /**
     * Adiciona/Soma mais quantidade ao estoque existente
     */
    @Transactional
    public EstoqueResponse adicionarQuantidade(UUID id, AdicionarEstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);

        int quantidadeAtual = estoque.getQuantidadeEstoque() != null ? estoque.getQuantidadeEstoque() : 0;
        estoque.setQuantidadeEstoque(quantidadeAtual + request.quantidade());

        estoque = estoqueRepository.save(estoque);
        return estoqueMapper.toResponse(estoque);
    }

    /**
     * Atualiza o valor unitário do item no estoque
     */
    @Transactional
    public EstoqueResponse atualizarValorUnitario(UUID id, AtualizarValorEstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.setValorUnitario(request.valorUnitario());

        estoque = estoqueRepository.save(estoque);
        return estoqueMapper.toResponse(estoque);
    }

    /**
     * Atualiza dados gerais do item de estoque (nome, marca, valor, etc.)
     */
    @Transactional
    public EstoqueResponse atualizar(UUID id, EstoqueRequest request) {
        Estoque estoque = buscarEntidadePorId(id);

        estoque.setNomeItem(request.nomeItem());
        estoque.setNomeMarca(request.nomeMarca());
        estoque.setValorUnitario(request.valorUnitario());
        estoque.setQuantidadeEstoque(request.quantidadeEstoque());
        estoque.setQuantidadeMinima(request.quantidadeMinima());
        estoque.setTipoCategoria(request.tipoCategoria());

        estoque = estoqueRepository.save(estoque);
        return estoqueMapper.toResponse(estoque);
    }

    private Estoque buscarEntidadePorId(UUID id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado com o ID: " + id));
    }
}