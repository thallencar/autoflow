package br.com.autoflow.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autoflow.application.validator.EnderecoValidator;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.ports.inbound.endereco.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnderecoUseCaseImpl implements
        CriarEnderecoUseCase,
        ListarEnderecosUseCase,
        BuscarEnderecoPorIdUseCase,
        AtualizarEnderecoUseCase,
        DeletarEnderecoUseCase {

    private static final String NOME_ENTIDADE = "Endereço";

    private final EnderecoRepositoryPort repositoryPort;
    private final EnderecoValidator enderecoValidator;

    @Override
    @Transactional
    public Endereco criar(Endereco endereco) {
        enderecoValidator.validarUf(endereco);
        return repositoryPort.save(endereco);
    }

    @Override
    public List<Endereco> listar() {
        return repositoryPort.findAll();
    }

    @Override
    public Endereco buscar(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
    }

    @Override
    @Transactional
    public Endereco atualizar(UUID id, Endereco enderecoParam) {
        Endereco enderecoExistente = buscar(id);

        enderecoValidator.validarUf(enderecoParam);

        enderecoExistente.atualizar( // Ajuste para o método de atualização do seu modelo de domínio Endereco, ou setadores manuais abaixo:
                enderecoParam.getCep(),
                enderecoParam.getUf(),
                enderecoParam.getCidade(),
                enderecoParam.getBairro(),
                enderecoParam.getLogradouro(),
                enderecoParam.getNumero(),
                enderecoParam.getComplemento()
        );

        return repositoryPort.save(enderecoExistente);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        Endereco endereco = buscar(id);
        repositoryPort.delete(endereco);
    }
}