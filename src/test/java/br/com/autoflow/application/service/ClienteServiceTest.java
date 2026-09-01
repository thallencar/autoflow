package br.com.autoflow.application.service;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.autoflow.application.dto.ClienteRequest;
import br.com.autoflow.application.dto.ClienteResponse;
import br.com.autoflow.application.dto.ClienteUpdateRequest;
import br.com.autoflow.application.dto.EnderecoRequest;
import br.com.autoflow.domain.enums.Genero;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Endereco;
import br.com.autoflow.domain.model.Usuario;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.EnderecoRepository;
import br.com.autoflow.domain.repository.UsuarioRepository;
import br.com.autoflow.domain.repository.VeiculoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import br.com.autoflow.infrastructure.mapper.ClienteMapper;
import br.com.autoflow.infrastructure.mapper.EnderecoMapper;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private EnderecoMapper enderecoMapper;

    @Mock
    private ClienteValidator clienteValidator;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Deve criar um cliente com sucesso e gerar usuário associado")
    void criarComSucesso() {
        // Arrange
        EnderecoRequest enderecoRequest = new EnderecoRequest(
                "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, "Apto 101"
        );

        ClienteRequest request = new ClienteRequest(
                "Teste da Silva",  "12345678901", "teste@email.com",LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, enderecoRequest
        );

        Endereco endereco = new Endereco();
        Cliente cliente = new Cliente();
        ClienteResponse responseDto = new ClienteResponse(UUID.randomUUID(), "Teste da Silva","12345678901","teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, null);

        when(enderecoMapper.toEntity(request.endereco())).thenReturn(endereco);
        when(enderecoRepository.save(endereco)).thenReturn(endereco);
        when(clienteMapper.toEntity(request, endereco)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(clienteMapper.toResponse(cliente)).thenReturn(responseDto);

        // Act
        ClienteResponse response = clienteService.criar(request);

        // Assert
        assertNotNull(response);
        verify(clienteValidator, times(1)).validarParaCriar(request);
        verify(enderecoRepository, times(1)).save(endereco);
        verify(clienteRepository, times(1)).save(cliente);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso")
    void listarComSucesso() {
        // Arrange
        Cliente cliente = new Cliente();
        ClienteResponse responseDto = new ClienteResponse(UUID.randomUUID(), "Teste da Silva","12345678901","teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, null);

        when(clienteRepository.findAll()).thenReturn(List.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(responseDto);

        // Act
        List<ClienteResponse> response = clienteService.listar();

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
        ClienteResponse responseDto = new ClienteResponse(id, "Teste da Silva","12345678901","teste@email.com", LocalDate.of(1995, 5, 15), "51999999999", Genero.OUTROS, null);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(responseDto);

        // Act
        ClienteResponse response = clienteService.buscarPorId(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.id());
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
        EnderecoRequest enderecoRequest = new EnderecoRequest(
                "93520-000", "RS", "Novo Hamburgo", "Centro", "Rua Principal", 100, null
        );
        ClienteUpdateRequest request = new ClienteUpdateRequest(
                "Novo Nome", "novo@email.com", "51988888888", Genero.OUTROS, enderecoRequest
        );

        Cliente cliente = new Cliente();
        ClienteResponse responseDto = new ClienteResponse(id, "Novo Nome", "12345678901", "novo@email.com", LocalDate.of(1995, 5, 15), "51988888888", Genero.OUTROS, null);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(usuarioRepository.findByCliente(cliente)).thenReturn(Optional.empty());
        when(clienteMapper.toResponse(cliente)).thenReturn(responseDto);

        // Act
        ClienteResponse response = clienteService.atualizar(id, request);

        // Assert
        assertNotNull(response);
        verify(clienteRepository, times(1)).save(cliente);
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
    void naoDeveDeletarClienteComVeiculosVinculados() {
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