package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.ClienteRequest;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteResponse;
import br.com.autoflow.adapters.inbound.controller.dto.ClienteUpdateRequest;
import br.com.autoflow.adapters.inbound.mapper.ClienteMapper;
import br.com.autoflow.application.validator.ClienteValidator;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.ports.inbound.cliente.*;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
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
    private final ClienteMapper clienteMapper;
    private final ClienteValidator clienteValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        clienteValidator.validarParaCriar(request);

        Endereco endereco = clienteMapper.toEnderecoDomain(request.endereco());
        endereco = enderecoRepository.save(endereco);

        Cliente cliente = clienteMapper.toDomain(request, endereco);
        cliente = clienteRepository.save(cliente);

        Perfil perfil = Perfil.CLIENTE;
        String senhaCriptografada = passwordEncoder.encode(cliente.getDocumento());
        Usuario usuario = Usuario.criarUsuarioParaCliente(cliente, perfil, senhaCriptografada);
        usuarioRepository.save(usuario);

        return clienteMapper.toResponse(cliente);
    }

    @Override
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Override
    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        return clienteMapper.toResponse(cliente);
    }

    @Override
    public ClienteResponse buscarPorDocumento(String documento) {
        Cliente cliente = clienteRepository.findByDocumento(documento)
                .orElseThrow(() -> new RegraNegocioException(NOME_ENTIDADE + " não encontrado: " + documento));
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse atualizar(UUID id, ClienteUpdateRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        // Atualização via domínio
        cliente.atualizarDados(
                request.nome(),
                request.telefone(),
                request.email(),
                request.genero(),
                request.endereco() != null ? clienteMapper.toEnderecoDomain(request.endereco()) : null
        );

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        usuarioRepository.findByCliente(clienteAtualizado)
                .ifPresent(usuario -> {
                    usuario.atualizarDadosAcesso(clienteAtualizado.getEmail(), Perfil.CLIENTE);
                    usuarioRepository.save(usuario);
                });

        return clienteMapper.toResponse(clienteAtualizado);
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