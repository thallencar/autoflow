package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.FuncionarioValidator;
import br.com.autoflow.domain.enums.Cargo;
import br.com.autoflow.domain.enums.Perfil;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.ports.inbound.funcionario.*;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.ports.outbound.FuncionarioRepositoryPort;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuncionarioUseCaseImpl implements
        CriarFuncionarioUseCase,
        ListarFuncionariosUseCase,
        BuscarFuncionarioPorIdUseCase,
        BuscarFuncionarioPorCpfUseCase,
        AtualizarFuncionarioUseCase,
        DeletarFuncionarioUseCase,
        RegistrarAdvertenciaFuncionarioUseCase {

    private final FuncionarioRepositoryPort repositoryPort;
    private final EnderecoRepositoryPort enderecoRepository;
    private final FuncionarioValidator funcionarioValidator;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private static final String NOME_ENTIDADE = "Funcionário";

    @Override
    @Transactional
    public Funcionario criar(Funcionario funcionario) {
        funcionarioValidator.validarParaCriar(funcionario);
        tratarEndereco(funcionario);

        Funcionario funcionarioSalvo = repositoryPort.save(funcionario);
        Perfil perfil = definirPerfilPorCargo(funcionarioSalvo.getCargo());
        String senhaCriptografada = passwordEncoder.encode(funcionarioSalvo.getCpf());

        Usuario usuario = Usuario.criarUsuarioParaFuncionario(funcionarioSalvo, perfil, senhaCriptografada);
        usuarioRepositoryPort.save(usuario);

        return funcionarioSalvo;
    }

    @Override
    public List<Funcionario> listar() {
        return repositoryPort.findAll();
    }

    @Override
    public Funcionario buscarPorId(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));
    }

    @Override
    public Funcionario buscarPorCpf(String cpf) {
        return repositoryPort.findByCpf(cpf)
                .orElseThrow(() -> new RegraNegocioException(NOME_ENTIDADE + " não encontrado com o CPF: " + cpf));
    }

    @Override
    @Transactional
    public Funcionario atualizar(UUID id, Funcionario funcionarioParam) {
        funcionarioValidator.validarParaAtualizar(id, funcionarioParam);

        Funcionario funcionario = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        tratarEndereco(funcionarioParam);

        funcionario.atualizar(
                funcionarioParam.getNome(),
                funcionarioParam.getTelefone(),
                funcionarioParam.getEmail(),
                funcionarioParam.getGenero(),
                funcionarioParam.getDataNascimento(),
                funcionarioParam.getCargo(),
                funcionarioParam.getEndereco()
        );

        repositoryPort.save(funcionario);

        usuarioRepositoryPort.findByFuncionarioId(id)
                .ifPresent(usuario -> {
                    usuario.atualizarDadosAcesso(funcionario.getEmail(), definirPerfilPorCargo(funcionario.getCargo()));
                    usuarioRepositoryPort.save(usuario);
                });

        return funcionario;
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        Funcionario funcionario = repositoryPort.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(NOME_ENTIDADE, id));

        usuarioRepositoryPort.findByFuncionarioId(id)
                .ifPresent(usuarioRepositoryPort::delete);

        repositoryPort.delete(funcionario);
    }

    @Override
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

    private void tratarEndereco(Funcionario funcionario) {
        if (funcionario.getEndereco() != null) {
            String cep = funcionario.getEndereco().getCep();
            Integer numero = funcionario.getEndereco().getNumero();

            Optional<Endereco> enderecoExistente = enderecoRepository.findByCepAndNumero(cep, numero);

            if (enderecoExistente.isPresent()) {
                funcionario.setEndereco(enderecoExistente.get());
            } else {
                funcionario.getEndereco().setId(null);
            }
        }
    }

    private Perfil definirPerfilPorCargo(Cargo cargo) {
        return switch (cargo) {
            case GERENTE, RECEPCIONISTA -> Perfil.ADMIN;
            case MECANICO, AUXILIAR_MECANICO -> Perfil.MECANICO;
        };
    }
}