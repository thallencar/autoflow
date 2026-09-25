package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.ClienteValidator;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.inbound.cliente.*;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteUseCaseImpl implements
        CriarClienteUseCase,
        ListarClientesUseCase,
        BuscarClientePorIdUseCase,
        BuscarClientePorDocumentoUseCase,
        AtualizarClienteUseCase,
        DeletarClienteUseCase {

    private static final String NOME_ENTIDADE = "Cliente";

    private final ClienteRepositoryPort clienteRepository;
    private final EnderecoRepositoryPort enderecoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VeiculoRepositoryPort veiculoRepository;
    private final ClienteValidator clienteValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Cliente criar(Cliente cliente) {
        clienteValidator.validarParaCriar(cliente);

        if (cliente.getEndereco() != null) {
            Endereco enderecoSalvo = enderecoRepository.save(cliente.getEndereco());
            cliente.setEndereco(enderecoSalvo);
        }

        Cliente clienteSalvo = clienteRepository.save(cliente);

        Perfil perfil = Perfil.CLIENTE;
        String senhaCriptografada = passwordEncoder.encode(clienteSalvo.getDocumento());
        Usuario usuario = Usuario.criarUsuarioParaCliente(clienteSalvo, perfil, senhaCriptografada);
        usuarioRepository.save(usuario);

        return clienteSalvo;
    }

    @Override
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
    }

    @Override
    public Cliente buscarPorDocumento(String documento) {
        return clienteRepository.findByDocumento(documento)
                .orElseThrow(() -> new RegraNegocioException(NOME_ENTIDADE + " não encontrado: " + documento));
    }

    @Override
    @Transactional
    public Cliente atualizar(UUID id, Cliente clienteAtualizadoParam) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        Endereco novoEndereco = clienteAtualizadoParam.getEndereco();
        if (novoEndereco != null) {
            novoEndereco = enderecoRepository.save(novoEndereco);
        }

        clienteExistente.atualizarDados(
                clienteAtualizadoParam.getNome(),
                clienteAtualizadoParam.getTelefone(),
                clienteAtualizadoParam.getEmail(),
                clienteAtualizadoParam.getGenero(),
                novoEndereco
        );

        Cliente clienteAtualizado = clienteRepository.save(clienteExistente);

        usuarioRepository.findByCliente(clienteAtualizado)
                .ifPresent(usuario -> {
                    usuario.atualizarDadosAcesso(clienteAtualizado.getEmail(), Perfil.CLIENTE);
                    usuarioRepository.save(usuario);
                });

        return clienteAtualizado;
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        if (veiculoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException("Não é possível excluir o cliente pois existem veículos vinculados a ele.");
        }

        usuarioRepository.findByCliente(cliente)
                .ifPresent(usuario -> {
                    usuarioRepository.delete(usuario);
                    usuarioRepository.flush();
                });

        clienteRepository.delete(cliente);
    }
}