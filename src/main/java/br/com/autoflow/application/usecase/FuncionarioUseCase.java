package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioRequest;
import br.com.autoflow.adapters.inbound.controller.dto.FuncionarioResponse;
import br.com.autoflow.adapters.inbound.mapper.FuncionarioMapper;
import br.com.autoflow.application.validator.FuncionarioValidator;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuncionarioUseCase {

    private final FuncionarioRepositoryPort repositoryPort;
    private final FuncionarioMapper funcionarioMapper;
    private final FuncionarioValidator funcionarioValidator;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private static final String NOME_ENTIDADE = "Funcionário";

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest request) {
        funcionarioValidator.validarParaCriar(request);

        Funcionario funcionario = funcionarioMapper.toDomain(request);
        funcionario = repositoryPort.save(funcionario);

        Perfil perfil = definirPerfilPorCargo(funcionario.getCargo());
        String senhaCriptografada = passwordEncoder.encode(funcionario.getCpf());

        Usuario usuario = Usuario.criarUsuarioParaFuncionario(funcionario, perfil, senhaCriptografada);
        usuarioRepositoryPort.save(usuario);

        return funcionarioMapper.toResponse(funcionario);
    }

    public List<FuncionarioResponse> listar() {
        return repositoryPort.findAll().stream()
                .map(funcionarioMapper::toResponse)
                .toList();
    }

    public FuncionarioResponse buscar(UUID id) {
        Funcionario funcionario = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
        return funcionarioMapper.toResponse(funcionario);
    }

    @Transactional
    public FuncionarioResponse atualizar(UUID id, FuncionarioRequest request) {
        funcionarioValidator.validarParaAtualizar(id, request);

        Funcionario funcionario = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        funcionario.atualizar(
                request.nome(),
                request.telefone(),
                request.email(),
                request.genero(),
                request.dataNascimento(),
                request.cargo(),
                null
        );

        repositoryPort.save(funcionario);

        usuarioRepositoryPort.findByFuncionarioId(id)
                .ifPresent(usuario -> {
                    usuario.atualizarDadosAcesso(funcionario.getEmail(), definirPerfilPorCargo(funcionario.getCargo()));
                    usuarioRepositoryPort.save(usuario);
                });

        return funcionarioMapper.toResponse(funcionario);
    }

    @Transactional
    public void deletar(UUID id) {
        Funcionario funcionario = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        usuarioRepositoryPort.findByFuncionarioId(id)
                .ifPresent(usuarioRepositoryPort::delete);

        repositoryPort.delete(funcionario);
    }

    @Transactional
    public String registrarAdvertencia(UUID id) {
        Funcionario funcionario = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        funcionario.adicionarAdvertencia();
        repositoryPort.save(funcionario);

        int totalAdvertencias = funcionario.getNrAdvertencias();

        if (funcionario.deveSerDemitido()) {
            return "Advertência registrada com sucesso. O funcionário atingiu " + totalAdvertencias +
                    " advertências e deve ser encaminhado para falar com a direção (Risco de demissão).";
        }

        return "Advertência registrada com sucesso. Total atual de advertências: " + totalAdvertencias;
    }

    private Perfil definirPerfilPorCargo(Cargo cargo) {
        return switch (cargo) {
            case GERENTE, RECEPCIONISTA -> Perfil.ADMIN;
            case MECANICO, AUXILIAR_MECANICO -> Perfil.MECANICO;
        };
    }
}