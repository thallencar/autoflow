package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.OrcamentoItemRequest;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.ports.inbound.estoque.*;
import br.com.autoflow.ports.outbound.EstoqueRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueUseCaseImpl implements
        CriarEstoqueUseCase,
        ListarEstoqueUseCase,
        BuscarEstoquePorIdUseCase,
        AtualizarEstoqueUseCase,
        GerenciarReservaEstoqueUseCase {

    private final EstoqueRepositoryPort repositoryPort;
    private static final String NOME_ENTIDADE = "Item de Estoque";

    @Override
    @Transactional
    public Estoque criar(Estoque estoque) {
        Estoque salvo = repositoryPort.save(estoque);
        salvo.deveDispararAlertaEstoqueBaixo();
        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estoque> listarTodos() {
        return repositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Estoque buscarPorId(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
    }

    @Override
    @Transactional
    public Estoque adicionarQuantidade(UUID id, Integer quantidade) {
        Estoque estoque = buscarPorId(id);
        estoque.adicionarQuantidade(quantidade);
        estoque.deveDispararAlertaEstoqueBaixo();
        return repositoryPort.save(estoque);
    }

    @Override
    @Transactional
    public Estoque atualizarValorUnitario(UUID id, BigDecimal valorUnitario) {
        Estoque estoque = buscarPorId(id);
        estoque.atualizarValorUnitario(valorUnitario);
        return repositoryPort.save(estoque);
    }

    @Override
    @Transactional
    public Estoque atualizar(UUID id, Estoque estoqueParam) {
        Estoque estoque = buscarPorId(id);
        estoque.atualizarDados(
                estoqueParam.getNomeItem(),
                estoqueParam.getNomeMarca(),
                estoqueParam.getValorUnitario(),
                estoqueParam.getQuantidadeEstoque(),
                estoqueParam.getQuantidadeMinima(),
                estoqueParam.getTipoCategoria()
        );
        estoque.deveDispararAlertaEstoqueBaixo();
        return repositoryPort.save(estoque);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estoque> listarInsumosComEstoqueBaixo() {
        return repositoryPort.findAll().stream()
                .filter(Estoque::deveDispararAlertaEstoqueBaixo)
                .toList();
    }

    @Override
    @Transactional
    public void reservarEstoqueParaItens(List<OrcamentoItemRequest> itensRequest) {
        if (itensRequest == null) return;

        for (OrcamentoItemRequest itemDto : itensRequest) {
            Estoque estoque = buscarPorId(itemDto.idEstoque());

            if (estoque.getQuantidadeEstoque() < itemDto.quantidade()) {
                throw new IllegalStateException("Estoque insuficiente para o item: " + estoque.getNomeItem());
            }

            estoque.removerQuantidade(itemDto.quantidade());
            repositoryPort.save(estoque);
        }
    }

    @Override
    @Transactional
    public void devolverEstoqueDeItens(List<OrcamentoItemRequest> itensRequest) {
        if (itensRequest == null) return;

        for (OrcamentoItemRequest itemDto : itensRequest) {
            Estoque estoque = buscarPorId(itemDto.idEstoque());
            estoque.adicionarQuantidade(itemDto.quantidade());
            repositoryPort.save(estoque);
        }
    }
}