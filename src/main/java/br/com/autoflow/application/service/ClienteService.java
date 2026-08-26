package br.com.autoflow.application.service;

import java.util.List;
import java.util.UUID;

import br.com.autoflow.application.dto.ClienteUpdateRequest;
import br.com.autoflow.domain.repository.VeiculoRepository;
import br.com.autoflow.exception.RegraNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.application.dto.ClienteResponse;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.EnderecoRepository;
import br.com.autoflow.domain.repository.UsuarioRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.infrastructure.mapper.ClienteMapper;
import br.com.autoflow.infrastructure.mapper.EnderecoMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {
    private final ClienteRepository respository;
    private final EnderecoRepository enderecoRepository;
    private final ClienteMapper clienteMapper;
    private final EnderecoMapper enderecoMapper;
    private final ClienteValidator clienteValidator;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final VeiculoRepository veiculoRepository;

    @Transactional
    public ClienteResponse criar(ClienteRequest request){
        clienteValidator.validarParaCriar(request);

        Endereco endereco = enderecoMapper.toEntity(request.endereco());
        endereco = enderecoRepository.save(endereco);

        Cliente cliente = clienteMapper.toEntity(request, endereco);
        cliente = respository.save(cliente);

        //criação automática de usuário
        Perfil perfil = Perfil.CLIENTE;
        Usuario usuario = Usuario.criarUsuarioParaCliente(cliente,perfil,passwordEncoder);
        usuarioRepository.save(usuario);
        
        return clienteMapper.toResponse(cliente);
    }

    public List<ClienteResponse> listar() {
        return respository.findAll()
                .stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente =
                respository.findById(id)
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));
        return clienteMapper.toResponse(cliente);
    }

    public ClienteResponse buscarPorDocumento(String documento) {
        Cliente cliente =
                respository.findByDocumento(documento)
                        .orElseThrow(() -> new RegraNegocioException("Cliente não encontrado: " + documento));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(UUID id, ClienteUpdateRequest request) {
        Cliente cliente = respository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));

        cliente.atualizarDados(request);
        
        Cliente clienteAtualizado = respository.save(cliente);

        usuarioRepository.findByCliente(clienteAtualizado)
                .ifPresent(usuario -> {
                    usuario.atualizarDadosAcesso(
                            clienteAtualizado.getEmail(),
                            Perfil.CLIENTE
                    );
                    usuarioRepository.save(usuario);
                });

        return clienteMapper.toResponse(clienteAtualizado);
    }

    @Transactional
    public void deletar(UUID id) {
        Cliente cliente = respository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));
        if (veiculoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException("Não é possível excluir o cliente pois existem veículos vinculados a ele.");
        }
        usuarioRepository.findByCliente(cliente)
                .ifPresent(usuario -> {
                    usuarioRepository.delete(usuario);
                    usuarioRepository.flush();
                });

        respository.delete(cliente);
    }
}
