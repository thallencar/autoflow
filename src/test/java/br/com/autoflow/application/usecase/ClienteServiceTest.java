package br.com.autoflow.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.autoflow.application.validator.ClienteValidator;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import br.com.autoflow.ports.outbound.EnderecoRepositoryPort;
import br.com.autoflow.ports.outbound.UsuarioRepositoryPort;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @Mock
    private EnderecoRepositoryPort enderecoRepository;

    @Mock
    private ClienteValidator clienteValidator;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VeiculoRepositoryPort veiculoRepository;

    @InjectMocks
    private ClienteUseCaseImpl clienteService;

    @Test
    @DisplayName("Deve criar um cliente com sucesso e gerar usuário associado")
    void criarComSucesso() {
        // Arrange
        Endereco endereco = new Endereco(
                UUID.randomUUID(),
                "93500-000",
                "RS",
                "Caxias do Sul",
                "Centro",
                "Rua Velha",
                100,
                "Apto 201"
        );

        Cliente cliente = new Cliente(
                UUID.randomUUID(),
                "Teste da Silva",
                "12345678901",
                "teste@email.com",
                LocalDate.of(1995, 5, 15),
                "51999999999",
                Genero.OUTROS,
                endereco
        );

        when(enderecoRepository.save(endereco)).thenReturn(endereco);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        // Act
        Cliente clienteSalvo = clienteService.criar(cliente);

        // Assert
        assertNotNull(clienteSalvo);
        verify(clienteValidator, times(1)).validarParaCriar(cliente);
        verify(enderecoRepository, times(1)).save(endereco);
        verify(clienteRepository, times(1)).save(cliente);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso")
    void listarComSucesso() {
        // Arrange
        Cliente cliente = new Cliente();
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        // Act
        List<Cliente> response = clienteService.listar();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void buscarPorIdComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(id);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        // Act
        Cliente response = clienteService.buscarPorId(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por ID inexistente")
    void buscarPorIdNaoEncontrado() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntidadeNaoEncontradaException.class, () -> clienteService.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void atualizarComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(id);

        Cliente clienteAtualizadoInput = new Cliente();
        clienteAtualizadoInput.setNome("Novo Nome");
        clienteAtualizadoInput.setEmail("novo@email.com");
        clienteAtualizadoInput.setTelefone("51988888888");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteExistente);
        when(usuarioRepository.findByCliente(clienteExistente)).thenReturn(Optional.empty());

        // Act
        Cliente response = clienteService.atualizar(id, clienteAtualizadoInput);

        // Assert
        assertNotNull(response);
        verify(clienteRepository, times(1)).save(clienteExistente);
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso quando não houver veículos vinculados")
    void deletarComSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.existsByClienteId(id)).thenReturn(false);
        when(usuarioRepository.findByCliente(cliente)).thenReturn(Optional.empty());

        // Act
        clienteService.deletar(id);

        // Assert
        verify(clienteRepository, times(1)).delete(cliente);
    }

    @Test
    @DisplayName("Não deve deletar cliente e lançar exceção se houver veículos vinculados")
    void naoDeveDeletarClienteWithVeiculosVinculados() {
        // Arrange
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.existsByClienteId(id)).thenReturn(true);

        // Act & Assert
        assertThrows(RegraNegocioException.class, () -> clienteService.deletar(id));
        verify(clienteRepository, never()).delete(any());
    }
}